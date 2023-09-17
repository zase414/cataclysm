package util;

public enum TexCoordinate {
    TL(new int[]{0, 0}), TR(new int[]{1, 0}), BL(new int[]{0, 1}), BR(new int[]{1, 1});
    public final int[] value;
    TexCoordinate(int[] value) {
        this.value = value;
    }
}
