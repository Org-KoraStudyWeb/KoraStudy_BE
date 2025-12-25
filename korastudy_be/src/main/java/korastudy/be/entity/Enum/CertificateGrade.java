package korastudy.be.entity.Enum;

public enum CertificateGrade {
    EXCELLENT("Xuất sắc", 90.0),
    GOOD("Giỏi", 80.0),
    FAIR("Khá", 70.0),
    PASS("Hoàn thành", 0.0);

    private final String displayName;
    private final double minScore;

    CertificateGrade(String displayName, double minScore) {
        this.displayName = displayName;
        this.minScore = minScore;
    }

    public static CertificateGrade fromScore(double score) {
        if (score >= EXCELLENT.minScore) return EXCELLENT;
        if (score >= GOOD.minScore) return GOOD;
        if (score >= FAIR.minScore) return FAIR;
        return PASS;
    }
}
