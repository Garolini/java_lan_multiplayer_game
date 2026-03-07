package io.github.java_lan_multiplayer.client.controllers.shipBuilding;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.effect.*;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class TileEffectManager {

    private final Map<ImageView, Timeline> activeTimelines = new HashMap<>();
    private final InnerShadow reservedEffect;
    private final DropShadow dropShadowEffect;

    public TileEffectManager() {
        dropShadowEffect = new DropShadow();
        dropShadowEffect.setColor(new Color(.2, .2, .2, .8));
        dropShadowEffect.setWidth(40);
        dropShadowEffect.setHeight(40);
        dropShadowEffect.setRadius(20);

        reservedEffect = new InnerShadow();
        reservedEffect.setBlurType(BlurType.ONE_PASS_BOX);
        reservedEffect.setColor(Color.GOLD);
        reservedEffect.setChoke(15);
        reservedEffect.setWidth(40);
        reservedEffect.setHeight(40);
        reservedEffect.setRadius(5);
    }

    public void applyDropShadowEffect(ImageView tile, boolean isReserved) {
        stopExistingAnimation(tile);

        tile.setEffect(isReserved ? combineWithReservedEffect(dropShadowEffect) : dropShadowEffect);
    }

    public void applySelectedEffect(ImageView tile, boolean isReserved) {
        stopExistingAnimation(tile);

        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.TRANSPARENT);
        innerShadow.setRadius(127);
        innerShadow.setBlurType(BlurType.GAUSSIAN);
        innerShadow.setChoke(1);
        innerShadow.setWidth(255);
        innerShadow.setHeight(255);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), _ -> {
            double time = (System.currentTimeMillis() % 2000) / 2000.0 * Math.PI * 2;
            double opacity = 0.2 + 0.1 * Math.sin(time);
            innerShadow.setColor(new Color(1, 1, 1, opacity));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        tile.setEffect(isReserved ? combineWithReservedEffect(innerShadow) : innerShadow);

        activeTimelines.put(tile, timeline);
    }

    public void applyReservedEffect(ImageView tile) {
        stopExistingAnimation(tile);

        tile.setEffect(reservedEffect);
    }

    private void stopExistingAnimation(ImageView tile) {
        if(!activeTimelines.containsKey(tile)) return;

        Timeline timeline = activeTimelines.get(tile);
        if (timeline != null) timeline.stop();
        activeTimelines.remove(tile);
    }

    private Blend combineWithReservedEffect(Effect baseEffect) {
        Blend blend = new Blend();
        blend.setBottomInput(baseEffect);
        blend.setTopInput(reservedEffect);
        return blend;
    }
}
