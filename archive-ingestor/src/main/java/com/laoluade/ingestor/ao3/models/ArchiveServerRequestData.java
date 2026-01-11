package com.laoluade.ingestor.ao3.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;


/**
 * <p>This class defines the POJO model for POST request data for the Archive Server.</p>
 */
@Getter
@ToString
@NoArgsConstructor
public class ArchiveServerRequestData {
    /**
     * <p>This attribute holds the page link the client wants to parse.</p>
     */
    private String pageLink;

    /**
     * <p>This attribute holds the client's nickname for the session.</p>
     */
    private String sessionNickname;

    /**
     * <p>This attribute holds the client's maximum comment thread depth.</p>
     */
    @Setter private int maxCommentThreadDepth;

    /**
     * <p>This attribute holds the client's maximum comment page limit.</p>
     */
    @Setter private int maxCommentPageLimit;

    /**
     * <p>This attribute holds the client's maximum kudos page limit.</p>
     */
    @Setter private int maxKudosPageLimit;

    /**
     * <p>This attribute holds the client's maximum bookmark page limit.</p>
     */
    @Setter private int maxBookmarkPageLimit;

    /**
     * <p>This attribute describes whether a session nickname was sent in the request.</p>
     */
    @Setter private boolean nicknameSent;

    /**
     * <p>This constructor creates a standardized request object with only a page link.</p>
     * @param pageLink The page link the client wants to parse.
     */
    public ArchiveServerRequestData(String pageLink) {
        createURLTest(pageLink);
        this.sessionNickname = "";
        this.maxCommentThreadDepth = -1;
        this.maxCommentPageLimit = -1;
        this.maxKudosPageLimit = -1;
        this.maxBookmarkPageLimit = -1;
        this.nicknameSent = false;
    }

    /**
     * <p>This constructor creates a standardized request object with only a page link and session nickname.</p>
     * @param pageLink The page link the client wants to parse.
     * @param sessionNickname The client's nickname for the session.
     */
    public ArchiveServerRequestData(String pageLink, String sessionNickname) {
        createURLTest(pageLink);
        createNicknameTest(sessionNickname);
        this.maxCommentThreadDepth = -1;
        this.maxCommentPageLimit = -1;
        this.maxKudosPageLimit = -1;
        this.maxBookmarkPageLimit = -1;

        if (!sessionNickname.isBlank()) { // If whitespace was sent, we can ignore and just reset it later
            this.nicknameSent = true;
        }
    }

    /**
     * <p>This constructor creates a standardized request object with all parameters except the session nickname.</p>
     * @param pageLink The page link the client wants to parse.
     * @param maxCommentThreadDepth The client's maximum comment thread depth.
     * @param maxCommentPageLimit The client's maximum comment page limit.
     * @param maxKudosPageLimit The client's maximum kudos page limit.
     * @param maxBookmarkPageLimit The client's maximum bookmark page limit.
     */
    public ArchiveServerRequestData(String pageLink, int maxCommentThreadDepth, int maxCommentPageLimit,
                                    int maxKudosPageLimit, int maxBookmarkPageLimit) {
        createURLTest(pageLink);
        this.sessionNickname = "";
        this.maxCommentThreadDepth = maxCommentThreadDepth;
        this.maxCommentPageLimit = maxCommentPageLimit;
        this.maxKudosPageLimit = maxKudosPageLimit;
        this.maxBookmarkPageLimit = maxBookmarkPageLimit;
        this.nicknameSent = false;
    }

    /**
     * <p>This constructor creates a standardized request object with all parameters.</p>
     * @param pageLink The page link the client wants to parse.
     * @param sessionNickname The client's nickname for the session.
     * @param maxCommentThreadDepth The client's maximum comment thread depth.
     * @param maxCommentPageLimit The client's maximum comment page limit.
     * @param maxKudosPageLimit The client's maximum kudos page limit.
     * @param maxBookmarkPageLimit The client's maximum bookmark page limit.
     */
    public ArchiveServerRequestData(String pageLink, String sessionNickname, int maxCommentThreadDepth,
                                    int maxCommentPageLimit, int maxKudosPageLimit, int maxBookmarkPageLimit) {
        createURLTest(pageLink);
        createNicknameTest(sessionNickname);
        this.maxCommentThreadDepth = maxCommentThreadDepth;
        this.maxCommentPageLimit = maxCommentPageLimit;
        this.maxKudosPageLimit = maxKudosPageLimit;
        this.maxBookmarkPageLimit = maxBookmarkPageLimit;

        if (!sessionNickname.isBlank()) { // If whitespace was sent, we can ignore and just reset it later
            this.nicknameSent = true;
        }
    }

    /**
     * <p>This method is a setter for the <code>pageLink</code> attribute.</p>
     * @param pageLink The page link the client wants to parse.
     */
    public void setPageLink(String pageLink) {
        createURLTest(pageLink);
    }

    /**
     * <p>This method is a setter for the <code>sessionNickname</code> attribute.</p>
     * @param sessionNickname The client's nickname for the session.
     */
    public void setSessionNickname(String sessionNickname) {
        createNicknameTest(sessionNickname);
    }

    /**
     * <p>
     *     This method validates that a page link's pattern lines up with an AO3 work/chapter and
     *     that it can be turned into a URL.
     * </p>
     * @param pageLink The page link the client wants to parse.
     */
    public void createURLTest(String pageLink) {
        try {
            new URI(pageLink).toURL();

            final Pattern storyLinkPattern = Pattern.compile(
                    "^https://archiveofourown\\.org/works/[0-9]+$", Pattern.CASE_INSENSITIVE
            );
            final Pattern chapterLinkPattern = Pattern.compile(
                    "^https://archiveofourown\\.org/works/[0-9]+/chapters/[0-9]+$", Pattern.CASE_INSENSITIVE
            );

            boolean isStoryLink = storyLinkPattern.matcher(pageLink).matches();
            boolean isChapterLink = chapterLinkPattern.matcher(pageLink).matches();

            if (isStoryLink || isChapterLink) {
                this.pageLink = pageLink;
            }
            else {
                this.pageLink = "";
            }
        }
        catch (IllegalArgumentException | URISyntaxException | MalformedURLException e) {
            this.pageLink = "";
        }
    }

    /**
     * <p>This method validates that a session nickname uses only alphanumeric characters, underscores, and hyphens.</p>
     * @param sessionNickname The client's nickname for the session.
     */
    public void createNicknameTest(String sessionNickname) {
        Pattern nicknamePattern = Pattern.compile("^[a-zA-Z0-9_-]*$", Pattern.CASE_INSENSITIVE);
        if (nicknamePattern.matcher(sessionNickname).matches()) {
            this.sessionNickname = sessionNickname;
        }
        else {
            this.sessionNickname = "";
        }
    }
}
