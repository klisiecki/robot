package pl.poznan.put.ioiorobot.widgets;

public interface JoystickMovedListener {
    public void OnMoved(int xPos, int yPos);

    public void OnReleased();
}
