# Exein Cosmo BB class


EXTRA_IMAGEDEPENDS += "cosmo-native"
IMAGE_POSTPROCESS_COMMAND += "cosmo;"


python cosmo() {
    import random
    import string
    import subprocess
    import tarfile
    from subprocess import Popen, PIPE, STDOUT

    FSTYPES = ["btrfs","container","cpio","cpio.gz","cpio.lz4","cpio.lzma","cpio.xz","cramfs","ext2","ext2.bz2",
        "ext2.gz","ext2.lzma","ext3","ext3.gz","ext4","ext4.gz","f2fs","hddimg","iso","jffs2","jffs2.sum",
        "multiubi","squashfs","squashfs-lz4","squashfs-lzo","squashfs-xz","ubi","ubifs","wic","wic.bz2",
        "wic.gz","wic.lzma"]
    FSTYPES_TAR = ["tar","tar.bz2","tar.gz", "tar.lz4","tar.xz","tar.zst"]

    API_SERVER = "https://cosmo-api.exein.io"
    TOPDIR = d.getVar("TOPDIR")
    ROOTFS_PATH = d.getVar("IMAGE_ROOTFS")
    BUILD_NAME = TOPDIR.split("/")[-1]
    COSMO_EXE = f"{TOPDIR}/../scripts/cosmo"
    PRJ_NAME = f"{BUILD_NAME}-{''.join(random.sample(string.ascii_letters, 8))}"
    
    bb.debug(1, "Image RootFS: ", ROOTFS_PATH)
    bb.plain("Exein Cosmo Project Name: ", PRJ_NAME)

    IMAGE_BASENAME= d.getVar("IMAGE_BASENAME")
    DEPLOY_DIR = f"deploy-{IMAGE_BASENAME}-image-complete"
    DEPLOY_PATH = f"{ROOTFS_PATH}/../{DEPLOY_DIR}"

    IMAGE_NAME = d.getVar("IMAGE_NAME")
    IMAGE_NAME_SUFFIX = d.getVar("IMAGE_NAME_SUFFIX")
    IMAGE_FSTYPES = d.getVar("IMAGE_FSTYPES").split(" ")

    STAGING_BINDIR_NATIVE = d.getVar("STAGING_BINDIR_NATIVE")

    for image_fstype in IMAGE_FSTYPES:
        if image_fstype in FSTYPES:
            IMAGE_PATH = f"{DEPLOY_PATH}/{IMAGE_NAME}{IMAGE_NAME_SUFFIX}.{image_fstype}"
            bb.debug(1, "Image path: ", IMAGE_PATH)
            try:
                CMD = COSMO_EXE + " --api-server " + API_SERVER + " create -f " + IMAGE_PATH + " -n " + PRJ_NAME + " -t linux -s yocto -d \"Yocto generated...\" "
                cli_cmd = subprocess.check_output(CMD, shell=True, stderr=subprocess.STDOUT)
                bb.plain(cli_cmd.decode("utf8"))
                
                break
            except subprocess.CalledProcessError as e:
                bb.error(e.output.decode("utf8"))
}
