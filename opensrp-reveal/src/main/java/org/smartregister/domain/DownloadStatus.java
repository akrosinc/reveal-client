package org.smartregister.domain;


public enum DownloadStatus implements Displayable {
    downloaded("Download successful"), nothingDownloaded("Nothing downloaded."), failedDownloaded(
            "Download failed.");

    private String displayValue;

    DownloadStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    @Override
    public String displayValue() {
        return displayValue;
    }
}