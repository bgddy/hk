package org.example.ui;

import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

/**
 * 支持手动控制的排序UI基类
 */
public abstract class ControllableSortUI {
    protected HBox root;
    protected Rectangle[] bars;
    protected SequentialTransition animation;
    protected int currentStep = 0;
    protected boolean isPlaying = false;
    
    protected static final double BAR_WIDTH = 50;
    protected static final double SCALE = 50;
    protected static final double BASELINE = 500;
    protected static final double SPACING = 5;

    public HBox getRoot() {
        return root;
    }

    /**
     * 自动播放排序动画
     */
    public abstract void visualizeSteps(long stepDelay);

    /**
     * 下一步
     */
    public abstract void nextStep();

    /**
     * 暂停
     */
    public void pause() {
        if (animation != null && animation.getStatus() == Animation.Status.RUNNING) {
            animation.pause();
            isPlaying = false;
        }
    }

    /**
     * 继续播放
     */
    public void play() {
        if (animation != null && animation.getStatus() == Animation.Status.PAUSED) {
            animation.play();
            isPlaying = true;
        }
    }

    /**
     * 重置
     */
    public abstract void reset();

    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * 获取当前步骤
     */
    public int getCurrentStep() {
        return currentStep;
    }

    /**
     * 获取总步骤数
     */
    public abstract int getTotalSteps();
}
