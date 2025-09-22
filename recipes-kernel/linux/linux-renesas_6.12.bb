DESCRIPTION = "Linux kernel for the R-Car board"

require recipes-kernel/linux/linux-yocto.inc

# LINUX_VERSION/REPO/BRANCH/SRCREV are defined in inc file
require recipes-kernel/linux/kernel_6.12.inc

COMPATIBLE_MACHINE = "(rcar-gen3|rcar-gen4)"

#KERNEL_VERSION_SANITY_SKIP = "1"
PV = "${LINUX_VERSION}+git${SRCPV}"

SRC_URI = "${REPO};branch=${BRANCH};protocol=https"
KERNEL_DEFCONFIG = "renesas_defconfig"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

S = "${WORKDIR}/git"

# For generating defconfig
KCONFIG_MODE = "--alldefconfig"
KBUILD_DEFCONFIG = "defconfig"

FILESEXTRAPATHS:prepend:sparrow-hawk = "${TOPDIR}/../../firmware:"
SRC_URI:append:sparrow-hawk = " \
    file://sparrow_hawk.cfg \
    file://sparrow-hawk-enable-i2c3-i2c4.dtsi;subdir=git/arch/arm64/boot/dts/renesas/ \
    file://0002-HACK-drivers-gpu-drm-drm_file-Ingnore-flag-checking.patch \
"

# Patchset for power management
SRC_URI:append:sparrow-hawk = " \
    file://0001-WIP-drivers-clk-r8a779g0-cpg-mssr-backport-from-BSP-.patch \
    file://0001-WIP-driver-pmdomain-r8a779g0-Backport-update.patch \
"

# UIO driver patchset
SRC_URI:append:sparrow-hawk = " \
    file://uio/0001-uio-Add-new-ioctl-for-power-management.patch \
    file://uio/0002-uio-Add-PMA-IOCTL-to-compat-list.patch \
    file://uio/0003-uio-uio_pdrv_genirq-renesas-Add-clock-divisor-ioctl-.patch \
    file://uio/0004-uio-Fix-the-logic-of-setting-reset-to-UIO-devices.patch \
    file://uio/0005-uio-Initialize-reset-struct-of-each-UIO-device.patch \
    file://uio/0006-uio-Inform-UIO-no-reset-line-to-users.patch \
    file://uio/0007-uio-Use-EOPNOTSUPP-not-ENOTSUPP.patch \
    file://uio/0008-uio-Switch-to-clk_hw_get_flags.patch \
    file://uio/0009-uio-Support-error-notification-to-upper-layer.patch \
    file://uio/0010-uio-uio_pdrv_genirq-Add-parameter-to-check-device-no.patch \
    file://uio/0011-uio-uio_pdrv_genirq-Add-parameter-error-in-case-not-.patch \
    file://uio/0012-uio-uio_pdrv_genirq-Hotfix-clock-and-power-control-f.patch \
    file://uio/0013-Revert-uio-uio_pdrv_genirq-Add-parameter-error-in-ca.patch \
    file://uio/0014-uio-uio_pdrv_genirq-Add-parameter-error-in-case-not-.patch \
    file://uio/0015-WIP-Fix-build-error-on-kernel-6.12.patch \
"



KBUILD_DEFCONFIG:sparrow-hawk = "renesas_defconfig"
KERNEL_DEVICETREE:append:sparrow-hawk = " \
    renesas/r8a779g3-sparrow-hawk-camera-j1-imx219.dtbo \
    renesas/r8a779g3-sparrow-hawk-camera-j2-imx219.dtbo \
    renesas/r8a779g3-sparrow-hawk-camera-j1-imx462.dtbo \
    renesas/r8a779g3-sparrow-hawk-camera-j2-imx462.dtbo \
    renesas/r8a779g3-sparrow-hawk-camera-j1-imx708.dtbo \
    renesas/r8a779g3-sparrow-hawk-camera-j2-imx708.dtbo \
    renesas/r8a779g3-sparrow-hawk-fan-pwm.dtbo \
    renesas/r8a779g3-sparrow-hawk-fan-argon40.dtbo \
    renesas/r8a779g3-sparrow-hawk-rpi-display-2-5in.dtbo \
    renesas/r8a779g3-sparrow-hawk-rpi-display-2-7in.dtbo \
"

do_compile:prepend:sparrow-hawk () {
    echo '#include "sparrow-hawk-enable-i2c3-i2c4.dtsi"' >>  ${S}/arch/arm64/boot/dts/renesas/r8a779g3-sparrow-hawk.dts
}

do_src_package_preprocess () {
    # Trim build paths from comments in generated sources to ensure reproducibility
    sed -i -e "s,${S}/,,g" \
            -e "s,${B}/,,g" \
        ${B}/drivers/video/logo/logo_linux_clut224.c \
        ${B}/drivers/tty/vt/consolemap_deftbl.c \
        ${B}/lib/oid_registry_data.c
}
addtask do_src_package_preprocess after do_compile before do_install

do_compile_kernelmodules:append () {
    if (grep -q -i -e '^CONFIG_MODULES=y$' ${B}/.config); then
        # 5.10+ kernels have module.lds that we need to copy for external module builds
        if [ -e "${B}/scripts/module.lds" ]; then
            install -Dm 0644 ${B}/scripts/module.lds ${STAGING_KERNEL_BUILDDIR}/scripts/module.lds
        fi
    fi
}

do_deploy:append() {
    # Remove the redundant device tree file (<device_tree>-<MACHINE>.dtb) that was created in the deploy directory
    for dtbf in ${KERNEL_DEVICETREE}; do
        dtb=`normalize_dtb "$dtbf"`
        dtb_ext=${dtb##*.}
        dtb_base_name=`basename $dtb .$dtb_ext`
        rm -f $deployDir/$dtb_base_name-${KERNEL_DTB_LINK_NAME}.$dtb_ext
    done
}
