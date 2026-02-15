package models ; 

public class DoubleUtils {
    
    // Get integer part (truncates toward zero)
    public static Integer getIntegerPart(Double value) {
        if (value == null) return null;
        return value.intValue();
    }
    
    // Get integer part (always floor)
    public static Integer getFloorPart(Double value) {
        if (value == null) return null;
        return (int) Math.floor(value);
    }
    
    // Get integer part (always ceil)
    public static Integer getCeilPart(Double value) {
        if (value == null) return null;
        return (int) Math.ceil(value);
    }
    
    // Get fractional part
    public static Double getFractionalPart(Double value) {
        if (value == null) return null;
        return value - Math.floor(value);  // Always positive fractional part
    }
    
    // Get fractional part (preserving sign)
    public static Double getFractionalPartSigned(Double value) {
        if (value == null) return null;
        return value % 1;
    }
    
    // Round to nearest integer
    public static Integer roundToInteger(Double value) {
        if (value == null) return null;
        return (int) Math.round(value);
    }

    // public static void main(String[] args) {
    //     Double x = -1.05555 ; 
    //     // System.out.println(getFractionalPartSigned(x) );
    //     // System.out.println(getFractionalPart(x) == 0.0);
    // }
}

// // Usage
// Double num = 3.333;
// System.out.println(DoubleUtils.getIntegerPart(num));      // 3
// System.out.println(DoubleUtils.getFloorPart(num));        // 3
// System.out.println(DoubleUtils.getCeilPart(num));         // 4
// System.out.println(DoubleUtils.getFractionalPart(num));   // 0.333
// System.out.println(DoubleUtils.roundToInteger(num));      // 3

// Double negative = -3.333;
// System.out.println(DoubleUtils.getIntegerPart(negative)); // -3 (truncates)
// System.out.println(DoubleUtils.getFloorPart(negative));   // -4 (always down)
// System.out.println(DoubleUtils.getFractionalPart(negative)); // 0.667 (positive)