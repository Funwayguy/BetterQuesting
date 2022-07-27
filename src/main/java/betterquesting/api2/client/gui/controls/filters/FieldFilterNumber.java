package betterquesting.api2.client.gui.controls.filters;

import betterquesting.api2.client.gui.controls.IFieldFilter;

// Generic number filter
public abstract class FieldFilterNumber<T extends Number> implements IFieldFilter<T> {
    public static final FieldFilterNumber<Double> DOUBLE = new FieldFilterNumber<Double>(true) {
        @Override
        public Double parseValue(String input) {
            try {
                return Double.parseDouble(input);
            } catch (Exception e) {
                return 0D;
            }
        }
    };
    public static final FieldFilterNumber<Float> FLOAT = new FieldFilterNumber<Float>(true) {
        @Override
        public Float parseValue(String input) {
            try {
                return Float.parseFloat(input);
            } catch (Exception e) {
                return 0F;
            }
        }
    };

    public static final FieldFilterNumber<Long> LONG = new FieldFilterNumber<Long>(false) {
        @Override
        public Long parseValue(String input) {
            try {
                return Long.parseLong(input);
            } catch (Exception e) {
                return 0L;
            }
        }
    };
    public static final FieldFilterNumber<Integer> INT = new FieldFilterNumber<Integer>(false) {
        @Override
        public Integer parseValue(String input) {
            try {
                return Integer.parseInt(input);
            } catch (Exception e) {
                return 0;
            }
        }
    };
    public static final FieldFilterNumber<Short> SHORT = new FieldFilterNumber<Short>(false) {
        @Override
        public Short parseValue(String input) {
            try {
                return Short.parseShort(input);
            } catch (Exception e) {
                return 0;
            }
        }
    };
    public static final FieldFilterNumber<Byte> BYTE = new FieldFilterNumber<Byte>(false) {
        @Override
        public Byte parseValue(String input) {
            try {
                return Byte.parseByte(input);
            } catch (Exception e) {
                return 0;
            }
        }
    };

    // === CLASS START ===

    private final boolean floating;

    public FieldFilterNumber(boolean floating) {
        this.floating = floating;
    }

    @Override
    public boolean isValid(String input) {
        return floating ? !input.matches("[^.0123456789-]") : !input.matches("[^0123456789-]");
    }

    @Override
    public String filterText(String input) {
        return floating ? input.replaceAll("[^.0123456789-]", "") : input.replaceAll("[^0123456789-]", "");
    }
}
