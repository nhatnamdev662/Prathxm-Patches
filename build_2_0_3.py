import os
import sys
import glob
import subprocess
import tempfile
import zipfile
import struct
import hashlib
import zlib

def main():
    base_mpp = r"C:\Users\MAY1\AppData\Local\Temp\patches-2.0.0.mpp"
    target_mpp = r"E:\chess mobile\project\patches-2.0.3.mpp"
    android_jar = r"C:\Users\MAY1\AppData\Local\Android\Sdk\platforms\android-35\android.jar"
    javac = r"C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot\bin\javac.exe"
    d8 = r"C:\Android\build-tools\android-37.0\d8.bat"
    kotlin_stdlib = r"C:\Users\MAY1\.gradle\caches\modules-2\files-2.1\org.jetbrains.kotlin\kotlin-stdlib\1.9.23\dbaadea1f5e68f790d242a91a38355a83ec38747\kotlin-stdlib-1.9.23.jar"

    print("Step 1: Compiling extension Java files for v2.0.3...")
    src_dir = r"E:\chess mobile\project\extensions\extension\src\main\java"
    java_files = glob.glob(os.path.join(src_dir, "**/*.java"), recursive=True)
    
    with tempfile.TemporaryDirectory() as td:
        build_config_dir = os.path.join(td, "build_config", "app", "prathxm", "chess", "extension")
        os.makedirs(build_config_dir, exist_ok=True)
        build_config_path = os.path.join(build_config_dir, "BuildConfig.java")
        with open(build_config_path, "w", encoding="utf-8") as bf:
            bf.write("""package app.prathxm.chess.extension;
public final class BuildConfig {
    public static final boolean DEBUG = false;
    public static final String LIBRARY_PACKAGE_NAME = "app.prathxm.chess.extension";
    public static final String BUILD_TYPE = "release";
    public static final String VERSION_NAME = "2.0.3";
    public static final String PATCH_VERSION = "2.0.3";
    public static final int VERSION_CODE = 20003;
}
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

        print("Step 2: Patching classes.dex at Hook 5 boundary with return-void...")
        with zipfile.ZipFile(base_mpp, "r") as bz:
            dex = bytearray(bz.read("classes.dex"))
            
            patch_off = 89376
            print(f"Original bytes at {patch_off}: {[hex(x) for x in dex[patch_off:patch_off+10]]}")
            # Inject return-void (0x0e 0x00) immediately after Hook 5
            dex[patch_off] = 0x0e
            dex[patch_off + 1] = 0x00
            print(f"Patched bytes at {patch_off}: {[hex(x) for x in dex[patch_off:patch_off+10]]}")

            # Recalculate SHA-1 (offset 0x0c, length 20, covers from 0x20 to EOF)
            sha1 = hashlib.sha1(dex[0x20:]).digest()
            dex[0x0c:0x20] = sha1

            # Recalculate Adler32 (offset 0x08, length 4, covers from 0x0c to EOF)
            adler = zlib.adler32(dex[0x0c:]) & 0xffffffff
            struct.pack_into("<I", dex, 0x08, adler)
            print("Recalculated SHA-1 and Adler32 checksums successfully.")

            print("Step 3: Writing final patches-2.0.3.mpp bundle (keeping all pristine .class files)...")
            with zipfile.ZipFile(target_mpp, "w", compression=zipfile.ZIP_DEFLATED) as oz:
                for n in bz.namelist():
                    if n == "classes.dex":
                        oz.writestr(n, dex)
                    elif n == "extensions/extension.mpe":
                        oz.writestr(n, new_extension_mpe)
                    else:
                        oz.writestr(n, bz.read(n))
            print(f"Bundle successfully created: {target_mpp} ({os.path.getsize(target_mpp)} bytes)")

            # Cache to temp
            temp_target = r"C:\Users\MAY1\AppData\Local\Temp\patches-2.0.3.mpp"
            with open(temp_target, "wb") as tf:
                with open(target_mpp, "rb") as sf:
                    tf.write(sf.read())
            print(f"Cached to {temp_target}")

if __name__ == "__main__":
    main()
