package com.example.benjamin.googlefirebasetest;


public class FingerPath {

    public Integer color;
    public Boolean emboss;
    public Boolean blur;
    public Integer strokeWidth;
    public Float x;
    public Float y;

    @Override
    public String toString() {
        return "FingerPath{" +
                "color=" + color +
                ", emboss=" + emboss +
                ", blur=" + blur +
                ", strokeWidth=" + strokeWidth +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    public FingerPath(){

    }

    public FingerPath(Integer color, Boolean emboss, Boolean blur, Integer strokeWidth, Float x, Float y) {
        this.color = color;
        this.emboss = emboss;
        this.blur = blur;
        this.strokeWidth = strokeWidth;
        this.x = x;
        this.y = y;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isEmboss() {
        return emboss;
    }

    public void setEmboss(boolean emboss) {
        this.emboss = emboss;
    }

    public boolean isBlur() {
        return blur;
    }

    public void setBlur(boolean blur) {
        this.blur = blur;
    }


}
