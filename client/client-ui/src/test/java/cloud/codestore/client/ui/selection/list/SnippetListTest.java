package cloud.codestore.client.ui.selection.list;

import cloud.codestore.client.usecases.listsnippets.ListSnippets;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import com.google.common.eventbus.EventBus;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@DisplayName("The snippet list")
class SnippetListTest {
    @Mock
    private ListSnippets listSnippets;
    @Mock
    private EventBus eventBus;
    private SnippetList snippetList;

    @Start
    private void start(Stage stage) throws Exception {
        when(listSnippets.list()).thenReturn(testItems());

        snippetList = new SnippetList(listSnippets, eventBus);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("snippetList.fxml"));
        fxmlLoader.setControllerFactory(controllerClass -> snippetList);
        Parent parent = fxmlLoader.load();

        stage.setScene(new Scene(parent));
        stage.show();
    }

    @Test
    @DisplayName("shows items provided by the ListSnippets use case")
    void showSnippets(FxRobot robot) {
        var items = listView(robot).getItems();
        assertThat(items).containsExactlyInAnyOrder(testItems().toArray(new SnippetListItem[0]));
    }

    @Test
    @DisplayName("triggers a SnippetSelectedEvent when a snippet is selected")
    void selectSnippet(FxRobot robot) {
        var argument = ArgumentCaptor.forClass(SnippetSelectedEvent.class);

        listView(robot).getSelectionModel().select(0);

        verify(eventBus).post(argument.capture());
        SnippetSelectedEvent event = argument.getValue();
        assertThat(event.snippetUri()).isEqualTo("http://localhost:8080/snippets/1");
    }

    private ListView<SnippetListItem> listView(FxRobot robot) {
        return robot.lookup("#list").queryListView();
    }

    private static List<SnippetListItem> testItems() {
        return List.of(
                new SnippetListItem("http://localhost:8080/snippets/1", "Snippet test #1"),
                new SnippetListItem("http://localhost:8080/snippets/2", "Snippet test #2"),
                new SnippetListItem("http://localhost:8080/snippets/3", "Snippet test #3"),
                new SnippetListItem("http://localhost:8080/snippets/4", "Snippet test #4"),
                new SnippetListItem("http://localhost:8080/snippets/5", "Snippet test #5"),
                new SnippetListItem("http://localhost:8080/snippets/6", "Snippet test #6"),
                new SnippetListItem("http://localhost:8080/snippets/7", "Snippet test #7"),
                new SnippetListItem("http://localhost:8080/snippets/8", "Snippet test #8"),
                new SnippetListItem("http://localhost:8080/snippets/9", "Snippet test #9"),
                new SnippetListItem("http://localhost:8080/snippets/10", "Snippet test #10")
        );
    }
}