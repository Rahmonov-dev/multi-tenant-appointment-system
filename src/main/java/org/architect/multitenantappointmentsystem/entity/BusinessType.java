package org.architect.multitenantappointmentsystem.entity;

public enum BusinessType {
    // ========== HEALTH & BEAUTY ==========
    DENTAL_CLINIC("Stomatologiya klinikasi", "🦷"),
    MEDICAL_CLINIC("Tibbiyot klinikasi", "🏥"),
    BEAUTY_SALON("Go'zallik saloni", "💅"),
    HAIR_SALON("Sartaroshxona (Unisex)", "💇"),
    BARBERSHOP("Barbershop (Erkaklar)", "💈"),
    NAIL_SALON("Manikür saloni", "💅"),
    SPA_CENTER("Spa markaz", "🧖"),
    MASSAGE_CENTER("Massaj markazi", "💆"),
    COSMETOLOGY("Kosmetologiya", "✨"),
    LASER_TREATMENT("Lazer muolaja", "⚡"),

    // ========== FITNESS & WELLNESS ==========
    FITNESS_CLUB("Fitnes klub", "🏋️"),
    YOGA_STUDIO("Yoga studiyasi", "🧘"),
    PILATES_STUDIO("Pilates studiyasi", "🤸"),
    GYM("Sport zal", "💪"),
    SWIMMING_POOL("Suzish havzasi", "🏊"),
    MARTIAL_ARTS("Jang san'ati", "🥋"),
    DANCE_STUDIO("Raqs studiyasi", "💃"),
    CROSSFIT("CrossFit", "🏋️‍♀️"),

    // ========== SPECIALIZED SERVICES ==========
    TATTOO_STUDIO("Tatuirovka studiyasi", "🎨"),
    PIERCING_STUDIO("Piercing studiya", "💎"),
    PHYSIOTHERAPY("Fizioterapiya", "🩺"),
    REHABILITATION("Reabilitatsiya markazi", "♿"),

    // ========== AUTOMOTIVE ==========
    CAR_WASH("Avtomoyka", "🚗"),
    AUTO_SERVICE("Avtoservis", "🔧"),
    CAR_DETAILING("Avtomobil deteyling", "✨"),
    TIRE_SERVICE("Shinomontaj", "🛞"),
    AUTO_PAINTING("Avtomobil bo'yash", "🎨"),

    // ========== PROFESSIONAL SERVICES ==========
    PHOTO_STUDIO("Foto studiya", "📸"),
    VIDEO_STUDIO("Video studiya", "🎥"),
    CONSULTING("Konsalting xizmatlari", "💼"),
    LEGAL_SERVICES("Yuridik xizmatlar", "⚖️"),
    ACCOUNTING("Buxgalteriya xizmatlari", "📊"),
    LANGUAGE_CENTER("Til o'rganish markazi", "📚"),
    TUTORING("Repetitorlik", "👨‍🏫"),
    MUSIC_SCHOOL("Musiqa maktabi", "🎵"),
    ART_STUDIO("San'at studiyasi", "🎨"),

    // ========== PET SERVICES ==========
    VETERINARY_CLINIC("Veterinariya klinikasi", "🐾")
    ,
    PET_GROOMING("Uy hayvonlari parvarishi", "🐕"),
    PET_HOTEL("Uy hayvonlari mehmonxonasi", "🏨"),
    PET_TRAINING("Hayvonlarni o'rgatish", "🐕‍🦺"),

    // ========== ENTERTAINMENT ==========
    EVENT_PLANNING("Tadbirlar tashkil etish", "🎉"),
    CONFERENCE_ROOM("Konferens zal", "🎤"),
    COWORKING_SPACE("Kovorking", "💻"),
    GAME_CENTER("O'yin markazi", "🎮"),
    ESCAPE_ROOM("Escape room", "🔐"),
    BOWLING("Bouling", "🎳"),
    CINEMA("Kinoteatr", "🎬"),

    // ========== FOOD & HOSPITALITY ==========
    RESTAURANT("Restoran", "🍽️"),
    CAFE("Kafe", "☕"),
    HOTEL("Mehmonxona", "🏨"),
    BANQUET_HALL("Banket zali", "🏛️"),

    // ========== OTHER ==========
    CUSTOM("Boshqa", "📋");

    private final String displayNameUz;
    private final String icon;

    BusinessType(String displayNameUz, String icon) {
        this.displayNameUz = displayNameUz;
        this.icon = icon;
    }

    public String getDisplayNameUz() {
        return displayNameUz;
    }

    public String getIcon() {
        return icon;
    }

    public String getDisplayNameWithIcon() {
        return icon + " " + displayNameUz;
    }
}
