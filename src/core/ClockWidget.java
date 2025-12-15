package core;

import javafx.animation.*;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.*;

/**
 * A reusable clock widget that can display time for any timezone
 */
public class ClockWidget extends VBox {
    private final String timeZoneId;
    private final Label timeZoneLabel;
    private final Label digitalClock;
    private Timeline hourTime;
    private Timeline minuteTime;
    private Timeline secondTime;
    private Timeline digitalTime;
    
    public ClockWidget(String timeZoneId, String displayName) {
        this.timeZoneId = timeZoneId;
        
        // Set up the layout
        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);
        this.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); " +
                      "-fx-background-radius: 15; " +
                      "-fx-padding: 15; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0, 0, 5);");

        timeZoneLabel = new Label(displayName);
        timeZoneLabel.setStyle("-fx-font-size: 16px; " +
                               "-fx-font-weight: bold; " +
                               "-fx-text-fill: white; " +
                               "-fx-font-family: 'Segoe UI', Arial, sans-serif;");

        final Circle face = new Circle(70, 70, 70);
        face.setStyle("-fx-fill: radial-gradient(radius 180%, #e8d4b8, derive(#e8d4b8, -30%), derive(#e8d4b8, 30%)); " +
                      "-fx-stroke: derive(#e8d4b8, -45%); " +
                      "-fx-stroke-width: 4; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 3, 3);");
        
        final Label brand = new Label("Elite Finds");
        brand.setStyle("-fx-font-size: 10px; -fx-text-fill: #333;");
        brand.setLayoutX(face.getCenterX() - 25);
        brand.setLayoutY(face.getCenterY() + 25);
        
        final Line hourHand = new Line(0, 0, 0, -35);
        hourHand.setTranslateX(70);
        hourHand.setTranslateY(70);
        hourHand.setStyle("-fx-stroke: #2f4f4f; -fx-stroke-width: 4; -fx-stroke-line-cap: round;");
        
        final Line minuteHand = new Line(0, 0, 0, -50);
        minuteHand.setTranslateX(70);
        minuteHand.setTranslateY(70);
        minuteHand.setStyle("-fx-stroke: derive(#2f4f4f, -5%); -fx-stroke-width: 3; -fx-stroke-line-cap: round;");
        
        final Line secondHand = new Line(0, 10, 0, -60);
        secondHand.setTranslateX(70);
        secondHand.setTranslateY(70);
        secondHand.setStyle("-fx-stroke: #b22222; -fx-stroke-width: 2; -fx-stroke-line-cap: round;");
        
        final Circle spindle = new Circle(70, 70, 4);
        spindle.setStyle("-fx-fill: derive(#2f4f4f, +5%);");

        javafx.scene.Group ticks = new javafx.scene.Group();
        for (int i = 0; i < 12; i++) {
            Line tick = new Line(0, -58, 0, -65);
            tick.setTranslateX(70);
            tick.setTranslateY(70);
            tick.setStyle("-fx-stroke: #b8860b; -fx-stroke-width: 2.5; -fx-stroke-line-cap: round;");
            tick.getTransforms().add(new Rotate(i * (360 / 12)));
            ticks.getChildren().add(tick);
        }
        
        final javafx.scene.Group analogueClock = new javafx.scene.Group(face, brand, ticks, spindle, hourHand, minuteHand, secondHand);

        digitalClock = new Label();
        digitalClock.setStyle("-fx-font-size: 14px; " +
                              "-fx-font-family: 'Courier New', monospace; " +
                              "-fx-text-fill: white; " +
                              "-fx-font-weight: bold;");
        Calendar calendar = getCalendarForTimeZone();
        final double seedSecondDegrees = calendar.get(Calendar.SECOND) * (360.0 / 60.0);
        final double seedMinuteDegrees = (calendar.get(Calendar.MINUTE) + seedSecondDegrees / 360.0) * (360.0 / 60.0);
        final double seedHourDegrees = (calendar.get(Calendar.HOUR) + seedMinuteDegrees / 360.0) * (360.0 / 12.0);
        final Rotate hourRotate = new Rotate(seedHourDegrees);
        final Rotate minuteRotate = new Rotate(seedMinuteDegrees);
        final Rotate secondRotate = new Rotate(seedSecondDegrees);
        hourHand.getTransforms().add(hourRotate);
        minuteHand.getTransforms().add(minuteRotate);
        secondHand.getTransforms().add(secondRotate);

        hourTime = new Timeline(
            new KeyFrame(
                Duration.hours(12),
                new KeyValue(hourRotate.angleProperty(), 360 + seedHourDegrees, Interpolator.LINEAR)
            )
        );

        minuteTime = new Timeline(
            new KeyFrame(
                Duration.minutes(60),
                new KeyValue(minuteRotate.angleProperty(), 360 + seedMinuteDegrees, Interpolator.LINEAR)
            )
        );

        secondTime = new Timeline(
            new KeyFrame(
                Duration.seconds(60),
                new KeyValue(secondRotate.angleProperty(), 360 + seedSecondDegrees, Interpolator.LINEAR)
            )
        );

        digitalTime = new Timeline(
            new KeyFrame(Duration.seconds(0),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        updateDigitalClock();
                    }
                }
            ),
            new KeyFrame(Duration.seconds(1))
        );
        
        hourTime.setCycleCount(Animation.INDEFINITE);
        minuteTime.setCycleCount(Animation.INDEFINITE);
        secondTime.setCycleCount(Animation.INDEFINITE);
        digitalTime.setCycleCount(Animation.INDEFINITE);

        final Glow glow = new Glow(0.3);
        analogueClock.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                analogueClock.setEffect(glow);
            }
        });
        analogueClock.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                analogueClock.setEffect(null);
            }
        });
        
        this.getChildren().addAll(timeZoneLabel, analogueClock, digitalClock);
    }
    

    public void start() {
        digitalTime.play();
        secondTime.play();
        minuteTime.play();
        hourTime.play();
    }

    public void stop() {
        if (digitalTime != null) digitalTime.stop();
        if (secondTime != null) secondTime.stop();
        if (minuteTime != null) minuteTime.stop();
        if (hourTime != null) hourTime.stop();
    }

    private Calendar getCalendarForTimeZone() {
        TimeZone tz = TimeZone.getTimeZone(timeZoneId);
        Calendar calendar = Calendar.getInstance(tz);
        return calendar;
    }

    private void updateDigitalClock() {
        Calendar calendar = getCalendarForTimeZone();
        String hourString = pad(2, '0', calendar.get(Calendar.HOUR) == 0 ? "12" : calendar.get(Calendar.HOUR) + "");
        String minuteString = pad(2, '0', calendar.get(Calendar.MINUTE) + "");
        String secondString = pad(2, '0', calendar.get(Calendar.SECOND) + "");
        String ampmString = calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
        digitalClock.setText(hourString + ":" + minuteString + ":" + secondString + " " + ampmString);
    }

    private String pad(int fieldWidth, char padChar, String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < fieldWidth; i++) {
            sb.append(padChar);
        }
        sb.append(s);
        return sb.toString();
    }
}