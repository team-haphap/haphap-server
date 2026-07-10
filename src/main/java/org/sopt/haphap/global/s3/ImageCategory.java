package org.sopt.haphap.global.s3;

public enum ImageCategory {
    LOGO_IMAGE("logo-images"),
    CARD_LOGO("card-logos"),
    IMAGE("images"),
    PASS_CARD("pass-cards"),
    BANNER("banners");

    private final String dirName;

    ImageCategory(String dirName) {
        this.dirName = dirName;
    }

    public String getDirName() {
        return dirName;
    }
}