package com.wikia.calabash.validation;

import lombok.Data;
import lombok.ToString;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class InEnumTests {
    public static void main(String[] args) {
        Model model = new Model();
        model.setStatus("aa");
        model.setPlatform(200000);
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<Model>> validate = validator.validate(model);
        List<String> messageList = new ArrayList<>();
        for (ConstraintViolation<Model> constraintViolation : validate) {
            messageList.add(constraintViolation.getMessage());
        }
        messageList.forEach(System.out::println);
    }

    @Data
    @ToString
    public static class Model {
        @InEnum(value = Status.class)
        private String status;
        @InEnum(value = Platform.class, field = "key")
        private int platform;

        public enum Status {
            OFF, ON
        }

        public enum Platform {
            Q_YIN(100000, "QQ音乐"),
            KU_GOU(100010, "酷狗"),
            KU_WO(100020, "酷我"),
            KK(100030, "全名K歌"),
            ;

            private int key;
            private String text;

            Platform(int key, String text) {
                this.key = key;
                this.text = text;
            }

            public int getKey() {
                return this.key;
            }

            public String getText() {
                return this.text;
            }

            public static String getText(int key) {
                Platform value = get(key);
                if (value != null) {
                    return value.text;
                }
                return null;
            }

            public static Platform get(int key) {
                for (Platform value : values()) {
                    if (value.key == key) {
                        return value;
                    }
                }
                return null;
            }

            public static Map<Integer, String> keyTexts() {
                Map<Integer, String> result = new HashMap<>();
                for (Platform value : values()) {
                    result.put(value.key, value.text);
                }
                return result;
            }
        }

    }
}
