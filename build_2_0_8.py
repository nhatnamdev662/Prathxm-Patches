import os
import sys
import glob
import subprocess
import tempfile
import zipfile
import time

def main():
    version = "2.0.8"
    version_code = 20008
    base_mpp = r"C:\Users\MAY1\AppData\Local\Temp\patches-2.0.0.mpp"
    target_mpp = rf"E:\chess mobile\project\patches-{version}.mpp"
    android_jar = r"C:\Users\MAY1\AppData\Local\Android\Sdk\platforms\android-35\android.jar"
    javac = r"C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot\bin\javac.exe"
    d8 = r"C:\Android\build-tools\android-37.0\d8.bat"
    kotlin_stdlib = r"C:\Users\MAY1\.gradle\caches\modules-2\files-2.1\org.jetbrains.kotlin\kotlin-stdlib\1.9.23\dbaadea1f5e68f790d242a91a38355a83ec38747\kotlin-stdlib-1.9.23.jar"

    print(f"Step 1: Compiling extension Java files for v{version}...")
    src_dir = r"E:\chess mobile\project\extensions\extension\src\main\java"
    java_files = glob.glob(os.path.join(src_dir, "**/*.java"), recursive=True)
    
    with tempfile.TemporaryDirectory() as td:
        build_config_dir = os.path.join(td, "build_config", "app", "prathxm", "chess", "extension")
        os.makedirs(build_config_dir, exist_ok=True)
        build_config_path = os.path.join(build_config_dir, "BuildConfig.java")
        with open(build_config_path, "w", encoding="utf-8") as bf:
            bf.write(f"""package app.prathxm.chess.extension;
public final class BuildConfig {{
    public static final boolean DEBUG = false;
    public static final String LIBRARY_PACKAGE_NAME = "app.prathxm.chess.extension";
    public static final String BUILD_TYPE = "release";
    public static final String VERSION_NAME = "{version}";
    public static final String PATCH_VERSION = "{version}";
    public static final int VERSION_CODE = {version_code};
}}
""")
        all_sources = java_files + [build_config_path]
        src_list_file = os.path.join(td, "sources.txt")
        with open(src_list_file, "w", encoding="utf-8") as f:
            for jf in all_sources:
                p = jf.replace("\\", "/")
                f.write(f'"{p}"\n')
                
        classes_out = os.path.join(td, "classes")
        os.makedirs(classes_out, exist_ok=True)
        cmd = [javac, "-encoding", "UTF-8", "-cp", android_jar, "-d", classes_out, f"@{src_list_file}"]
        res = subprocess.run(cmd, capture_output=True, text=True)
        if res.returncode != 0:
            print("javac failed:", res.stderr)
            sys.exit(1)
        print(f"javac succeeded on {len(all_sources)} source files.")
        
        ext_jar = os.path.join(td, "ext_classes.jar")
        with zipfile.ZipFile(ext_jar, "w") as jz:
            for root, dirs, files in os.walk(classes_out):
                for file in files:
                    full_p = os.path.join(root, file)
                    rel_p = os.path.relpath(full_p, classes_out).replace("\\", "/")
                    jz.write(full_p, rel_p)
                    
        dex_out = os.path.join(td, "dex_out")
        os.makedirs(dex_out, exist_ok=True)
        d8_cmd = [d8, "--lib", android_jar, "--output", dex_out, ext_jar, kotlin_stdlib]
        d8_res = subprocess.run(d8_cmd, capture_output=True, text=True)
        if d8_res.returncode != 0:
            print("d8 failed:", d8_res.stderr)
            sys.exit(1)
            
        with open(os.path.join(dex_out, "classes.dex"), "rb") as f:
            new_extension_mpe = f.read()
        print(f"extension.mpe built successfully: {len(new_extension_mpe)} bytes (header: {new_extension_mpe[:8]})")

        print("Step 2: Clean JVM bytecode patching for StockfishPatchKt.class...")
        with zipfile.ZipFile(base_mpp, "r") as bz:
            b = bytearray(bz.read("app/prathxm/chess/patches/stockfish/StockfishPatchKt.class"))
            
            # Immediately return Unit.INSTANCE after Hook 5:
            # 0xb2 0x00 0x34: getstatic kotlin/Unit.INSTANCE:Lkotlin/Unit;
            # 0xb0: areturn
            b[16345] = 0xb2
            b[16346] = 0x00
            b[16347] = 0x34
            b[16348] = 0xb0
            print("Patched StockfishPatchKt.class with clean return Unit.INSTANCE after Hook 5.")

            print("Step 3: Compiling root classes.dex cleanly with official d8...")
            patch_jar = os.path.join(td, "patch_classes.jar")
            with zipfile.ZipFile(patch_jar, "w") as pj:
                for n in bz.namelist():
                    if n == "app/prathxm/chess/patches/stockfish/StockfishPatchKt.class":
                        pj.writestr(n, b)
                    elif n.endswith(".class"):
                        pj.writestr(n, bz.read(n))

            root_dex_out = os.path.join(td, "root_dex")
            os.makedirs(root_dex_out, exist_ok=True)
            rd8_cmd = [d8, "--lib", android_jar, "--output", root_dex_out, patch_jar]
            rd8_res = subprocess.run(rd8_cmd, capture_output=True, text=True)
            if rd8_res.returncode != 0:
                print("Root d8 failed:", rd8_res.stderr)
                sys.exit(1)

            with open(os.path.join(root_dex_out, "classes.dex"), "rb") as f:
                new_root_dex = f.read()
            print(f"Root classes.dex compiled cleanly with d8: {len(new_root_dex)} bytes")

            print(f"Step 4: Writing clean patches-{version}.mpp bundle...")
            now_ms = int(time.time() * 1000)
            manifest_content = f"""Manifest-Version: 1.0
Name: NNVC Patches
Description: NNVC Patches for Chess.com - Ad-free & local offline engine
Version: {version}
Timestamp: {now_ms}
Source: https://github.com/nhatnamdev662/Prathxm-Patches
Author: NNVC
Website: https://github.com/nhatnamdev662/Prathxm-Patches
License: GPLv3
Patcher-Version: 1.5.2

"""

            with zipfile.ZipFile(target_mpp, "w", compression=zipfile.ZIP_DEFLATED) as oz:
                for n in bz.namelist():
                    if n == "META-INF/MANIFEST.MF":
                        oz.writestr(n, manifest_content.encode("utf-8"))
                    elif n == "app/prathxm/chess/patches/stockfish/StockfishPatchKt.class":
                        oz.writestr(n, b)
                    elif n == "classes.dex":
                        oz.writestr(n, new_root_dex)
                    elif n == "extensions/extension.mpe":
                        oz.writestr(n, new_extension_mpe)
                    else:
                        oz.writestr(n, bz.read(n))
            print(f"Bundle successfully created: {target_mpp} ({os.path.getsize(target_mpp)} bytes)")

            # Cache to temp
            temp_target = rf"C:\Users\MAY1\AppData\Local\Temp\patches-{version}.mpp"
            with open(temp_target, "wb") as tf:
                with open(target_mpp, "rb") as sf:
                    tf.write(sf.read())
            print(f"Cached to {temp_target}")

if __name__ == "__main__":
    main()
