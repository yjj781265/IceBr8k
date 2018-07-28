package app.jayang.icebr8k.Utility;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import uk.co.samuelwall.materialtaptargetprompt.extras.PromptOptions;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.CirclePromptBackground;

public class DimmedPromptBackground extends CirclePromptBackground
{
    private RectF dimBounds = new RectF();
    private Paint dimPaint;

    public DimmedPromptBackground()
    {
        dimPaint = new Paint();
        dimPaint.setColor(Color.BLACK);
    }

    @Override
    public void prepare(@NonNull final PromptOptions options, final boolean clipToBounds, @NonNull Rect clipBounds)
    {
        super.prepare(options, clipToBounds, clipBounds);
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        dimBounds.set(0, 0, metrics.widthPixels, metrics.heightPixels);
    }

    @Override
    public void update(@NonNull final PromptOptions options, float revealModifier, float alphaModifier)
    {
        super.update(options, revealModifier, alphaModifier);
        // Change the 200 as need to change how dark the dim is
        this.dimPaint.setAlpha((int) (200 * alphaModifier));
    }

    @Override
    public void draw(@NonNull Canvas canvas)
    {
        canvas.drawRect(this.dimBounds, this.dimPaint);
        super.draw(canvas);
    }
}