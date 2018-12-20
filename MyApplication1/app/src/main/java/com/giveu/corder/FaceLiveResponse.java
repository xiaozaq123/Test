package com.giveu.corder;

public class FaceLiveResponse {

    /**
     * time_used : 1243
     * verification : {"idcard":{"confidence":94.726166,"thresholds":{"1e-3":62.168713,"1e-5":74.39926,"1e-4":69.31534,"1e-6":78.038055}}}
     * request_id : 1542680668,1c2d67b1-592a-40e3-9490-2b42770755d1
     * images : {"image_best":"/9j/4AAzRiNmM2NDgzZmI4M2ZiYjFmNGZh"}
     * biz_no :
     * result_message : SUCCESS
     * result_code : 1000
     * attack_result : {"threshold":0.5,"score":6.360308E-6,"result":false}
     */

    public int time_used;
    public VerificationBean verification;
    public String request_id;
    public ImagesBean images;
    public String biz_no;
    public String result_message;
    public int result_code;
    public AttackResultBean attack_result;

    public static class VerificationBean {
        /**
         * idcard : {"confidence":94.726166,"thresholds":{"1e-3":62.168713,"1e-5":74.39926,"1e-4":69.31534,"1e-6":78.038055}}
         */

        public IdcardBean idcard;

        public static class IdcardBean {
            /**
             * confidence : 94.726166
             * thresholds : {"1e-3":62.168713,"1e-5":74.39926,"1e-4":69.31534,"1e-6":78.038055}
             */

            public double confidence;
       //     public ThresholdsBean thresholds;

//            public static class ThresholdsBean {
//                /**
//                 * 1e-3 : 62.168713
//                 * 1e-5 : 74.39926
//                 * 1e-4 : 69.31534
//                 * 1e-6 : 78.038055
//                 */
//
//                @com.google.gson.annotations.SerializedName("1e-3")
//                public double _$1e3;
//                @com.google.gson.annotations.SerializedName("1e-5")
//                public double _$1e5;
//                @com.google.gson.annotations.SerializedName("1e-4")
//                public double _$1e4;
//                @com.google.gson.annotations.SerializedName("1e-6")
//                public double _$1e6;
//            }
        }
    }

    public static class ImagesBean {
        /**
         * image_best : /9j/4AAzRiNmM2NDgzZmI4M2ZiYjFmNGZh
         */

        public String image_best;
    }

    public static class AttackResultBean {
        /**
         * threshold : 0.5
         * score : 6.360308E-6
         * result : false
         */

        public double threshold;
        public double score;
        public boolean result;
    }
}
