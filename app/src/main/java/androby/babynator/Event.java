package androby.babynator;

import android.widget.ImageButton;

/**
 * Created by Ouassila on 06/02/2017.
 */

public class Event {
    private ImageButton btn_delete;
    private String date;
    private String text;
    private int id;

    public Event(int id, ImageButton btn_delete, String date, String text) {
        this.btn_delete = btn_delete;
        this.date = date;
        this.text = text;
        this.id = id;
    }

    public ImageButton getBtn_delete() {
        return btn_delete;
    }

    public void setBtn_delete(ImageButton btn_delete) {
        this.btn_delete = btn_delete;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
