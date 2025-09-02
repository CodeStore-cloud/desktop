package cloud.codestore.client.ui.snippet;

import javafx.beans.property.SimpleStringProperty;

/**
 * A {@link SimpleStringProperty} that does not directly set the given value when calling {@link #set(String)} or {@link #setValue(String)}.
 * Instead, the change is requested and the final value is set asynchronously at a later point in time.
 */
class AsyncStringProperty extends SimpleStringProperty {
    interface ChangeRequestHandler {
        void requestValueChange(String value);
    }

    private ChangeRequestHandler changeHandler = value -> {};

    AsyncStringProperty() {
        super("");
    }

    /**
     * Requests setting the given value.
     * @param value the requested value.
     */
    @Override
    public void set(String value) {
        changeHandler.requestValueChange(value);
    }

    /**
     * Finally sets the new value.
     * @param value the new value.
     */
    void setFinally(String value) {
        super.set(value);
    }

    void onChangeRequested(ChangeRequestHandler requestChangeHandler) {
        this.changeHandler = requestChangeHandler;
    }
}
