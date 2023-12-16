package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.CoreException;

/**
 * Exception in case the given page number is out of the bounds of available pages.
 */
public class PageNotExistsException extends CoreException {
    private final int pageNumber;

    PageNotExistsException(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
