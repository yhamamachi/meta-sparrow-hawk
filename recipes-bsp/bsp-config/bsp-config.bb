SUMMARY = "BSP configuration (default/ADAS) script"
LICENSE = "CLOSED"

SRC_URI = " \
    file://bsp-config_v4h.sh \
"

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${UNPACKDIR}/*.sh ${D}${bindir}/
}

