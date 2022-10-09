package simplicial.software.utilities.android;

import android.graphics.PointF;
import android.view.MotionEvent;

public class InputController {
    private final PointF p1 = new PointF();
    private final PointF p2 = new PointF();
    private final PointF prevP1 = new PointF();
    private final PointF prevP2 = new PointF();
    private final int eventBoundWidth;
    private final int eventBoundHeight;
    int p1PointerID = -1;
    int p2PointerID = -1;
    private boolean p1Active = false;
    private boolean p2Active = false;
    private float distance = 0;
    private float prevDistance = 0;

    public InputController(int eventBoundWidth, int eventBoundHeight) {
        this.eventBoundWidth = eventBoundWidth;
        this.eventBoundHeight = eventBoundHeight;
    }

    public boolean update(MotionEvent motionEvent) {
        boolean handled = false;
        prevP1.set(p1);
        prevP2.set(p2);
        prevDistance = distance;

        for (int i = 0; i < motionEvent.getPointerCount(); i++) {
            int action = motionEvent.getActionMasked();
            int pointerID = motionEvent.getPointerId(i);
            int actionPointerIndex = motionEvent.getActionIndex();

            float x = motionEvent.getX(i) / eventBoundWidth;
            float y = motionEvent.getY(i) / eventBoundHeight;

            if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) && i == actionPointerIndex) {
                if (!p1Active) {
                    p1Active = true;
                    p1PointerID = pointerID;
                    p1.set(x, y);
                    prevP1.set(p1);
                    handled = true;
                } else if (!p2Active) {
                    p2Active = true;
                    p2PointerID = pointerID;
                    p2.set(x, y);
                    prevP1.set(p2);
                    distance =
                            (float) Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
                    prevDistance = distance;
                    handled = true;
                }
            }

            if (action == MotionEvent.ACTION_MOVE && pointerID == p1PointerID && p1Active) {
                p1.set(x, y);
                handled = true;
            }
            if (action == MotionEvent.ACTION_MOVE && pointerID == p2PointerID && p2Active) {
                p2.set(x, y);
                handled = true;
            }

            if (action == MotionEvent.ACTION_MOVE && p2Active) {
                distance =
                        (float) Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
                handled = true;
            }

            if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) && i == actionPointerIndex) {
                if (p2Active) {
                    if (pointerID == p1PointerID) {
                        p1.set(p2);
                        p1PointerID = p2PointerID;
                    }

                    prevP1.set(p1);

                    p2Active = false;
                    p2PointerID = -1;
                    handled = true;
                } else if (p1Active) {
                    p1Active = false;
                    p1PointerID = -1;
                    handled = true;
                }

                distance = 0;
                prevDistance = 0;
            }
        }
        return handled;
    }

    public PointF getPointer1() {
        if (p1Active)
            return p1;
        return null;
    }

    public PointF getPointer2() {
        if (p2Active)
            return p2;
        return null;
    }

    public PointF getPointer1Movement() {
        return new PointF(p1.x - prevP1.x, p1.y - prevP1.y);
    }

    public float getPointerDistanceChange() {
        return distance - prevDistance;
    }
}
