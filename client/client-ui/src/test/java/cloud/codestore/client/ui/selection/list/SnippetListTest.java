package cloud.codestore.client.ui.selection.list;

import cloud.codestore.client.usecases.listsnippets.ListSnippets;
import cloud.codestore.client.usecases.listsnippets.SnippetListItem;
import cloud.codestore.client.usecases.listsnippets.SnippetPage;
import com.google.common.eventbus.EventBus;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@DisplayName("The snippet list")
class SnippetListTest {
    private static final String NEXT_PAGE_URL = "http://localhost:8080/snippets?page[number]=2";

    @Mock
    private ListSnippets listSnippets;
    @Mock
    private EventBus eventBus;
    private SnippetList snippetList;

    @Start
    private void start(Stage stage) throws Exception {
        var expectedSnippets = testItems();
        var testPage = new SnippetPage(expectedSnippets, NEXT_PAGE_URL);
        when(listSnippets.readSnippets()).thenReturn(testPage);

        snippetList = new SnippetList(listSnippets, eventBus);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("uiMessages");
        URL fxmlFile = getClass().getResource("snippetList.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
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
        assertThat(event.snippetUri()).isEqualTo(uri(1));
    }

    @Test
    @DisplayName("shows a button when more pages are available")
    void showOrHidePaginationButton(FxRobot robot) {
        var page2 = new SnippetPage(Collections.emptyList(), "");
        when(listSnippets.readSnippets(NEXT_PAGE_URL)).thenReturn(page2);

        var showMoreButton = showMoreButton(robot);
        assertThat(showMoreButton.isVisible()).isTrue();
        robot.clickOn(showMoreButton);
        assertThat(showMoreButton.isVisible()).isFalse();
    }

    @Test
    @DisplayName("loads the next page of snippets when pressing \"show more\"")
    void loadNextPage(FxRobot robot) {
        var page2 = new SnippetPage(page2TestItems(), "");
        when(listSnippets.readSnippets(NEXT_PAGE_URL)).thenReturn(page2);
        var listView = listView(robot);
        assertThat(listView.getItems()).hasSize(10);

        var showMoreButton = showMoreButton(robot);
        robot.clickOn(showMoreButton);

        assertThat(listView.getItems()).hasSize(20);
    }

    private ListView<SnippetListItem> listView(FxRobot robot) {
        return robot.lookup("#list").queryListView();
    }

    private Node showMoreButton(FxRobot robot) {
        return robot.lookup("#nextPage").queryLabeled();
    }

    private static List<SnippetListItem> testItems() {
        return List.of(
                new SnippetListItem(uri(1), "Snippet test #11"),
                new SnippetListItem(uri(2), "Snippet test #12"),
                new SnippetListItem(uri(3), "Snippet test #13"),
                new SnippetListItem(uri(4), "Snippet test #14"),
                new SnippetListItem(uri(5), "Snippet test #15"),
                new SnippetListItem(uri(6), "Snippet test #16"),
                new SnippetListItem(uri(7), "Snippet test #17"),
                new SnippetListItem(uri(8), "Snippet test #18"),
                new SnippetListItem(uri(9), "Snippet test #19"),
                new SnippetListItem(uri(10), "Snippet test #20")
        );
    }

    private static List<SnippetListItem> page2TestItems() {
        return List.of(
                new SnippetListItem(uri(11), "Snippet test #1"),
                new SnippetListItem(uri(12), "Snippet test #2"),
                new SnippetListItem(uri(13), "Snippet test #3"),
                new SnippetListItem(uri(14), "Snippet test #4"),
                new SnippetListItem(uri(15), "Snippet test #5"),
                new SnippetListItem(uri(16), "Snippet test #6"),
                new SnippetListItem(uri(17), "Snippet test #7"),
                new SnippetListItem(uri(18), "Snippet test #8"),
                new SnippetListItem(uri(19), "Snippet test #9"),
                new SnippetListItem(uri(20), "Snippet test #10")
        );
    }

    private static String uri(int snippetId) {
        return "http://localhost:8080/snippets/" + snippetId;
    }
}