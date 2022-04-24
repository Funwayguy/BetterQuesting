package betterquesting.api2.client.gui.misc;

import org.lwjgl.util.vector.ReadableVector4f;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;

/**
 * Provides pre-made anchor points for GUIs with functions to quickly create new ones
 */
public class GuiAlign {
    public static final ReadableVector4f FULL_BOX = new ImmutableVec4f(0F, 0F, 1F, 1F);

    public static final ReadableVector4f TOP_LEFT = new ImmutableVec4f(0F, 0F, 0F, 0F);
    public static final ReadableVector4f TOP_CENTER = new ImmutableVec4f(0.5F, 0F, 0.5F, 0F);
    public static final ReadableVector4f TOP_RIGHT = new ImmutableVec4f(1F, 0F, 1F, 0F);
    public static final ReadableVector4f TOP_EDGE = new ImmutableVec4f(0F, 0F, 1F, 0F);

    public static final ReadableVector4f MID_LEFT = new ImmutableVec4f(0F, 0.5F, 0F, 0.5F);
    public static final ReadableVector4f MID_CENTER = new ImmutableVec4f(0.5F, 0.5F, 0.5F, 0.5F);
    public static final ReadableVector4f MID_RIGHT = new ImmutableVec4f(1F, 0.5F, 1F, 0.5F);

    public static final ReadableVector4f BOTTOM_LEFT = new ImmutableVec4f(0F, 1F, 0F, 1F);
    public static final ReadableVector4f BOTTOM_CENTER = new ImmutableVec4f(0.5F, 1F, 0.5F, 1F);
    public static final ReadableVector4f BOTTOM_RIGHT = new ImmutableVec4f(1F, 1F, 1F, 1F);
    public static final ReadableVector4f BOTTOM_EDGE = new ImmutableVec4f(0F, 1F, 1F, 1F);

    public static final ReadableVector4f HALF_LEFT = new ImmutableVec4f(0F, 0F, 0.5F, 1F);
    public static final ReadableVector4f HALF_RIGHT = new ImmutableVec4f(0.5F, 0F, 1F, 1F);
    public static final ReadableVector4f HALF_TOP = new ImmutableVec4f(0F, 0F, 1F, 0.5F);
    public static final ReadableVector4f HALF_BOTTOM = new ImmutableVec4f(0F, 0.5F, 1F, 1F);

    public static final ReadableVector4f LEFT_EDGE = new ImmutableVec4f(0F, 0F, 0F, 1F);
    public static final ReadableVector4f RIGHT_EDGE = new ImmutableVec4f(1F, 0F, 1F, 1F);

    /**
     * Takes two readable Vector4f points and merges them in a single Vector4f anchor region
     */
    public static Vector4f quickAnchor(ReadableVector4f v1, ReadableVector4f v2) {
        float x1 = Math.min(v1.getX(), v2.getX());
        float y1 = Math.min(v1.getY(), v2.getY());
        float x2 = Math.max(v1.getZ(), v2.getZ());
        float y2 = Math.max(v1.getW(), v2.getW());

        return new Vector4f(x1, y1, x2, y2);
    }

    private static class ImmutableVec4f implements ReadableVector4f {
        private final ReadableVector4f v4f;

        public ImmutableVec4f(float x, float y, float z, float w) {
            this.v4f = new Vector4f(x, y, z, w);
        }

        @Override
        public float getZ() {
            return v4f.getZ();
        }

        @Override
        public float getX() {
            return v4f.getX();
        }

        @Override
        public float getY() {
            return v4f.getY();
        }

        @Override
        public float length() {
            return v4f.length();
        }

        @Override
        public float lengthSquared() {
            return v4f.lengthSquared();
        }

        @Override
        public Vector store(FloatBuffer buf) {
            return v4f.store(buf);
        }

        @Override
        public float getW() {
            return v4f.getW();
        }
    }
}
