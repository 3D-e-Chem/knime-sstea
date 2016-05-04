package nl.esciencecenter.e3dchem.sstea;

public class Score {
    private int seqPos;
    private double score;
    private double entropyInside;
    private double entropyOutside;
    private int variabilityInside;
    private int variabilityOutside;

    public Score(int seqPos, double score, double entropyInside, double entropyOutside, int variabilityInside,
            int variabilityOutside) {
        super();
        this.seqPos = seqPos;
        this.score = score;
        this.entropyInside = entropyInside;
        this.entropyOutside = entropyOutside;
        this.variabilityInside = variabilityInside;
        this.variabilityOutside = variabilityOutside;
    }

    public int getSeqPos() {
        return seqPos;
    }

    public void setSeqPos(int seqPos) {
        this.seqPos = seqPos;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getEntropyInside() {
        return entropyInside;
    }

    public void setEntropyInside(double entropyInside) {
        this.entropyInside = entropyInside;
    }

    public double getEntropyOutside() {
        return entropyOutside;
    }

    public void setEntropyOutside(double entropyOutside) {
        this.entropyOutside = entropyOutside;
    }

    public int getVariabilityInside() {
        return variabilityInside;
    }

    public void setVariabilityInside(int variabilityInside) {
        this.variabilityInside = variabilityInside;
    }

    public int getVariabilityOutside() {
        return variabilityOutside;
    }

    public void setVariabilityOutside(int variabilityOutside) {
        this.variabilityOutside = variabilityOutside;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(entropyInside);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(entropyOutside);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(score);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + seqPos;
        result = prime * result + variabilityInside;
        result = prime * result + variabilityOutside;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Score other = (Score) obj;
        if (Double.doubleToLongBits(entropyInside) != Double.doubleToLongBits(other.entropyInside))
            return false;
        if (Double.doubleToLongBits(entropyOutside) != Double.doubleToLongBits(other.entropyOutside))
            return false;
        if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
            return false;
        if (seqPos != other.seqPos)
            return false;
        if (variabilityInside != other.variabilityInside)
            return false;
        if (variabilityOutside != other.variabilityOutside)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Score [seqPos=" + seqPos + ", score=" + score + ", entropyInside=" + entropyInside + ", entropyOutside="
                + entropyOutside + ", variabilityInside=" + variabilityInside + ", variabilityOutside=" + variabilityOutside
                + "]";
    }

}
