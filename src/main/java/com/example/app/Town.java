package com.example.app;

import java.util.ArrayList;

public class Town implements Comparable<Town> {
    public int id;
    public String name;
    public int x;
    public int y;
    public static ArrayList<Float> FofX = new ArrayList<>();
    public static ArrayList<Float> GofX = new ArrayList<>();
    public Town(int id, String name, int x, int y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public float hDistance(Town target) {
        double distance = Math.sqrt(Math.pow(this.x - target.x, 2) + Math.pow(this.y - target.y, 2));
        return (float)(distance / 39.5);
    }

    @Override
    public int compareTo(Town o) {
        return Float.compare(FofX.get(this.id), FofX.get(o.id));
    }
}
