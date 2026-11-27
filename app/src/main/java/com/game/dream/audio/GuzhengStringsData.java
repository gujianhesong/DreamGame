package com.game.dream.audio;

import java.util.List;

/**
 * {@code assets/guzheng/guzheng_21_strings.json}
 */
public class GuzhengStringsData {

    public List<StringEntry> strings;
    public ArticulationMidPluck articulationMidPluck;

    public static class StringEntry {
        public int no;
        public int midi;
        public String note;
        public String sample;
        public int sampleMidi;
        public float rate;
        public List<String> rr;
    }

    public static class ArticulationMidPluck {
        public String sampleMidi55Mid;
        public String sampleMidi67Mid;
    }
}