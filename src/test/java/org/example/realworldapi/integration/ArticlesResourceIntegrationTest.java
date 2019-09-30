package org.example.realworldapi.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.http.HttpStatus;
import org.example.realworldapi.DatabaseIntegrationTest;
import org.example.realworldapi.domain.builder.ArticleBuilder;
import org.example.realworldapi.domain.entity.persistent.*;
import org.example.realworldapi.domain.security.Role;
import org.example.realworldapi.domain.service.JWTService;
import org.example.realworldapi.util.InsertResult;
import org.example.realworldapi.util.UserUtils;
import org.example.realworldapi.web.dto.NewArticleDTO;
import org.example.realworldapi.web.dto.NewCommentDTO;
import org.example.realworldapi.web.dto.UpdateArticleDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.TemporalType;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.example.realworldapi.constants.TestConstants.*;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ArticlesResourceIntegrationTest extends DatabaseIntegrationTest {

  private final String ARTICLES_PATH = API_PREFIX + "/articles";
  private final String FEED_PATH = ARTICLES_PATH + "/feed";

  @Inject private ObjectMapper objectMapper;
  @Inject private JWTService jwtService;
  @Inject private Slugify slugify;

  @BeforeEach
  public void beforeEach() {
    clear();
  }

  @Test
  public void shouldReturn401WhenExecuteFeedEndpointWithoutAuthorization() {

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .queryParam("offset", 0)
        .queryParam("limit", 5)
        .get(FEED_PATH)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED)
        .body("errors.body", hasItem("UNAUTHORIZED"));
  }

  @Test
  public void
      given10ArticlesForLoggedUser_whenExecuteFeedEndpointWithOffset0AndLimit5_shouldReturnListOf5Articles() {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);

    User follower1 =
        createUser("follower1", "follower1@mail.com", "bio", "image", "follower1_123", Role.USER);

    InsertResult<Article> insertResult = new InsertResult<>();

    createArticles(follower1, "Title", "Description", "Body", 10, insertResult);

    Tag tag1 = createTag("Tag 1");

    Tag tag2 = createTag("Tag 2");

    createArticlesTags(insertResult.getResults(), tag1, tag2);

    User user = createUser("user", "user@mail.com", "bio", "image", "user123", Role.USER);

    createArticles(user, "Title", "Description", "Body", 4, insertResult);

    follow(loggedUser, follower1);

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .queryParam("offset", 0)
        .queryParam("limit", 5)
        .get(FEED_PATH)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "articles[0]",
            hasKey("slug"),
            "articles[0]",
            hasKey("title"),
            "articles[0]",
            hasKey("description"),
            "articles[0]",
            hasKey("body"),
            "articles[0].tagList.size()",
            is(2),
            "articles[0].tagList",
            hasItems(tag1.getName(), tag2.getName()),
            "articles[0]",
            hasKey("createdAt"),
            "articles[0]",
            hasKey("updatedAt"),
            "articles[0]",
            hasKey("favorited"),
            "articles[0]",
            hasKey("favoritesCount"),
            "articles[0]",
            hasKey("author"),
            "articlesCount",
            is(5));
  }

  @Test
  public void
      given8ArticlesForLoggedUser_whenExecuteFeedEndpointWithOffset0AndLimit10_shouldReturnListOf8Articles() {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);

    User follower1 =
        createUser("follower1", "follower1@mail.com", "bio", "image", "follower1_123", Role.USER);

    InsertResult<Article> insertResult = new InsertResult<>();

    createArticles(follower1, "Title", "Description", "Body", 8, insertResult);

    User user = createUser("user", "user@mail.com", "bio", "image", "user123", Role.USER);

    createArticles(user, "Title", "Description", "Body", 4, insertResult);

    follow(loggedUser, follower1);

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .queryParam("offset", 0)
        .queryParam("limit", 10)
        .get(FEED_PATH)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "articles[0]",
            hasKey("slug"),
            "articles[0]",
            hasKey("title"),
            "articles[0]",
            hasKey("description"),
            "articles[0]",
            hasKey("body"),
            "articles[0]",
            hasKey("tagList"),
            "articles[0]",
            hasKey("createdAt"),
            "articles[0]",
            hasKey("updatedAt"),
            "articles[0]",
            hasKey("favorited"),
            "articles[0]",
            hasKey("favoritesCount"),
            "articles[0]",
            hasKey("author"),
            "articlesCount",
            is(8));
  }

  @Test
  public void
      given9ArticlesForLoggedUser_whenExecuteFeedEndpointWithOffset0AndLimit10_shouldReturnListOf9Articles() {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);

    User follower1 =
        createUser("follower1", "follower1@mail.com", "bio", "image", "follower1_123", Role.USER);

    InsertResult<Article> insertResult = new InsertResult<>();

    createArticles(follower1, "Title", "Description", "Body", 5, insertResult);

    User user = createUser("user", "user@mail.com", "bio", "image", "user123", Role.USER);

    createArticles(user, "Title", "Description", "Body", 4, insertResult);

    follow(loggedUser, follower1);

    follow(loggedUser, user);

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .queryParam("offset", 0)
        .queryParam("limit", 10)
        .get(FEED_PATH)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "articles[0]",
            hasKey("slug"),
            "articles[0]",
            hasKey("title"),
            "articles[0]",
            hasKey("description"),
            "articles[0]",
            hasKey("body"),
            "articles[0]",
            hasKey("tagList"),
            "articles[0]",
            hasKey("createdAt"),
            "articles[0]",
            hasKey("updatedAt"),
            "articles[0]",
            hasKey("favorited"),
            "articles[0]",
            hasKey("favoritesCount"),
            "articles[0]",
            hasKey("author"),
            "articlesCount",
            is(9));
  }

  @Test
  public void
      given20ArticlesForLoggedUser_whenExecuteFeedEndpointWithOffset0AndLimit10_shouldReturnListOf10Articles() {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);

    User follower1 =
        createUser("follower1", "follower1@mail.com", "bio", "image", "follower1_123", Role.USER);

    InsertResult<Article> insertResult = new InsertResult<>();

    createArticles(follower1, "Title", "Description", "Body", 2, insertResult);

    User user = createUser("user", "user@mail.com", "bio", "image", "user123", Role.USER);

    createArticles(user, "Title", "Description", "Body", 18, insertResult);

    follow(loggedUser, follower1);

    follow(loggedUser, user);

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .queryParam("offset", 0)
        .queryParam("limit", 10)
        .get(FEED_PATH)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "articles[0]",
            hasKey("slug"),
            "articles[0]",
            hasKey("title"),
            "articles[0]",
            hasKey("description"),
            "articles[0]",
            hasKey("body"),
            "articles[0]",
            hasKey("tagList"),
            "articles[0]",
            hasKey("createdAt"),
            "articles[0]",
            hasKey("updatedAt"),
            "articles[0]",
            hasKey("favorited"),
            "articles[0]",
            hasKey("favoritesCount"),
            "articles[0]",
            hasKey("author"),
            "articlesCount",
            is(10));
  }

  @Test
  public void
      given10ArticlesWithDifferentTags_whenExecuteGlobalArticlesEndpoint_shouldReturn5Articles() {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);

    InsertResult<Article> insertResult = new InsertResult<>();

    createArticles(loggedUser, "Title", "Description", "Body", 5, insertResult);

    Tag tag1 = createTag("Tag 1");

    createArticlesTags(insertResult.getResults(), tag1);

    insertResult = new InsertResult<>(insertResult.getNextValue());

    createArticles(loggedUser, "Title", "Description", "Body", 5, insertResult);

    Tag tag2 = createTag("Tag 2");

    createArticlesTags(insertResult.getResults(), tag2);

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .queryParam("offset", 0)
        .queryParam("limit", 10)
        .queryParam("tag", tag1.getName())
        .get(ARTICLES_PATH)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "articles[0]",
            hasKey("slug"),
            "articles[0]",
            hasKey("title"),
            "articles[0]",
            hasKey("description"),
            "articles[0]",
            hasKey("body"),
            "articles[0].tagList",
            hasItem(tag1.getName()),
            "articles[0]",
            hasKey("createdAt"),
            "articles[0]",
            hasKey("updatedAt"),
            "articles[0]",
            hasKey("favorited"),
            "articles[0]",
            hasKey("favoritesCount"),
            "articles[0]",
            hasKey("author"),
            "articlesCount",
            is(5));
  }

  @Test
  public void shouldReturn401WhenExecuteCreateArticleEndpointWithoutToken() {

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .post(ARTICLES_PATH)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test
  public void
      givenValidArticleRequestWithoutTags_whenExecuteCreateArticleEndpoint_shouldReturnACreatedArticle()
          throws JsonProcessingException {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);

    NewArticleDTO newArticleDTO = createNewArticle("Title", "Description", "Body");

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .body(objectMapper.writeValueAsString(newArticleDTO))
        .post(ARTICLES_PATH)
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body(
            "article.size()",
            is(10),
            "article",
            hasKey("slug"),
            "article.title",
            is(newArticleDTO.getTitle()),
            "article.description",
            is(newArticleDTO.getDescription()),
            "article.body",
            is(newArticleDTO.getBody()),
            "article",
            hasKey("tagList"),
            "article",
            hasKey("createdAt"),
            "article",
            hasKey("updatedAt"),
            "article",
            hasKey("favorited"),
            "article",
            hasKey("favoritesCount"),
            "article",
            hasKey("author"));
  }

  @Test
  public void
      givenValidArticleRequestWithTags_whenExecuteCreateArticleEndpoint_shouldReturnACreatedArticle()
          throws JsonProcessingException {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);

    Tag tag1 = createTag("Tag 1");
    Tag tag2 = createTag("Tag 2");
    String tag3 = "Tag 3";
    String tag4 = "Tag 4";

    NewArticleDTO newArticleDTO =
        createNewArticle(
            "Title 1", "Description", "Body", tag1.getName(), tag2.getName(), tag3, tag4);

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .body(objectMapper.writeValueAsString(newArticleDTO))
        .post(ARTICLES_PATH)
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body(
            "article.size()",
            is(10),
            "article",
            hasKey("slug"),
            "article.title",
            is(newArticleDTO.getTitle()),
            "article.description",
            is(newArticleDTO.getDescription()),
            "article.body",
            is(newArticleDTO.getBody()),
            "article.tagList",
            hasItems(tag1.getName(), tag2.getName(), tag3, tag4),
            "article",
            hasKey("createdAt"),
            "article",
            hasKey("updatedAt"),
            "article",
            hasKey("favorited"),
            "article",
            hasKey("favoritesCount"),
            "article",
            hasKey("author"));
  }

  @Test
  public void
      givenExistentArticle_whenExecuteGetArticleBySlugEndpoint_shouldReturnArticleWithStatusCode200() {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);
    Article article = createArticle(loggedUser, "Title", "Description", "Body");

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .pathParam("slug", article.getSlug())
        .get(ARTICLES_PATH + "/{slug}")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "article.title",
            is(article.getTitle()),
            "article.description",
            is(article.getDescription()),
            "article.body",
            is(article.getBody()));
  }

  @Test
  public void
      givenExistentArticle_whenExecuteUpdateArticleEndpoint_shouldReturnUpdatedArticleWithStatusCode200()
          throws JsonProcessingException {
    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);
    Article article = createArticle(loggedUser, "Title", "Description", "Body");

    UpdateArticleDTO updateArticleDTO = new UpdateArticleDTO();
    updateArticleDTO.setTitle("updated title");
    updateArticleDTO.setDescription("updated description");
    updateArticleDTO.setBody("updated body");

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .body(objectMapper.writeValueAsString(updateArticleDTO))
        .pathParam("slug", article.getSlug())
        .put(ARTICLES_PATH + "/{slug}")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "article.title",
            is(updateArticleDTO.getTitle()),
            "article.description",
            is(updateArticleDTO.getDescription()),
            "article.body",
            is(updateArticleDTO.getBody()));
  }

  @Test
  public void givenExistentArticle_whenExecuteDeleteArticleEndpoint_shouldReturnStatusCode200() {
    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);
    Article article = createArticle(loggedUser, "Title", "Description", "Body");

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .pathParam("slug", article.getSlug())
        .delete(ARTICLES_PATH + "/{slug}")
        .then()
        .statusCode(HttpStatus.SC_OK);

    Assertions.assertNull(transaction(() -> entityManager.find(Article.class, article.getId())));
  }

  @Test
  public void
      givenExistentArticleWithComments_whenExecuteGetCommentsBySlugEndpoint_shouldReturnCommentWithStatusCode200() {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);
    Article article = createArticle(loggedUser, "Title", "Description", "Body");

    createComment(loggedUser, article, "comment1");
    createComment(loggedUser, article, "comment2");

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .pathParam("slug", article.getSlug())
        .get(ARTICLES_PATH + "/{slug}/comments")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "comments.size()",
            is(2),
            "comments[0]",
            hasKey("id"),
            "comments[0]",
            hasKey("createdAt"),
            "comments[0]",
            hasKey("updatedAt"),
            "comments[0]",
            hasKey("body"),
            "comments[0]",
            hasKey("author"));
  }

  @Test
  public void
      givenExistentArticleWithoutComments_whenExecuteCreateCommentEndpoint_shouldReturnCommentWithStatusCode200()
          throws JsonProcessingException {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);
    Article article = createArticle(loggedUser, "Title", "Description", "Body");

    NewCommentDTO newCommentDTO = new NewCommentDTO();
    newCommentDTO.setBody("comment body");

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .pathParam("slug", article.getSlug())
        .body(objectMapper.writeValueAsString(newCommentDTO))
        .post(ARTICLES_PATH + "/{slug}/comments")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "comment.size()",
            is(5),
            "comment",
            hasKey("id"),
            "comment",
            hasKey("createdAt"),
            "comment",
            hasKey("updatedAt"),
            "comment.body",
            is(newCommentDTO.getBody()),
            "comment.author.username",
            is(loggedUser.getUsername()));
  }

  @Test
  public void
      givenExistentArticleWithComments_whenExecuteDeleteCommentEndpoint_shouldReturnStatusCode200() {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);

    User user = createUser("user", "user@mail.com", "bio", "image", "user123", Role.USER);

    Article article = createArticle(user, "Title", "Description", "Body");

    Comment comment1 = createComment(loggedUser, article, "comment 1 body");
    createComment(loggedUser, article, "comment 2 body");

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .pathParam("slug", article.getSlug())
        .pathParam("id", comment1.getId())
        .delete(ARTICLES_PATH + "/{slug}/comments/{id}")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void
      givenExistentArticle_whenExecuteFaroriteArticleEndpoint_shouldReturnFavoritedArticleWithStatusCode200() {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);

    User user = createUser("user", "user@mail.com", "bio", "image", "user123", Role.USER);

    Article article = createArticle(user, "Title", "Description", "Body");

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .pathParam("slug", article.getSlug())
        .post(ARTICLES_PATH + "/{slug}/favorite")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "article.size()",
            is(10),
            "article",
            hasKey("slug"),
            "article.title",
            is(article.getTitle()),
            "article.description",
            is(article.getDescription()),
            "article.body",
            is(article.getBody()),
            "article",
            hasKey("tagList"),
            "article",
            hasKey("createdAt"),
            "article",
            hasKey("updatedAt"),
            "article.favorited",
            is(true),
            "article.favoritesCount",
            is(1),
            "article",
            hasKey("author"));
  }

  @Test
  public void
      givenExistentArticleFavorited_whenExecuteUnfaroriteArticleEndpoint_shouldReturnUnfavoritedArticleWithStatusCode200() {

    User loggedUser =
        createUser("loggedUser", "loggeduser@mail.com", "bio", "image", "loggeduser123", Role.USER);

    User user = createUser("user", "user@mail.com", "bio", "image", "user123", Role.USER);

    Article article = createArticle(user, "Title", "Description", "Body");

    favorite(article, loggedUser);

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + loggedUser.getToken())
        .pathParam("slug", article.getSlug())
        .delete(ARTICLES_PATH + "/{slug}/favorite")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "article.size()",
            is(10),
            "article",
            hasKey("slug"),
            "article.title",
            is(article.getTitle()),
            "article.description",
            is(article.getDescription()),
            "article.body",
            is(article.getBody()),
            "article",
            hasKey("tagList"),
            "article",
            hasKey("createdAt"),
            "article",
            hasKey("updatedAt"),
            "article.favorited",
            is(false),
            "article.favoritesCount",
            is(0),
            "article",
            hasKey("author"));
  }

  private ArticlesUsers favorite(Article article, User user) {
    return transaction(
        () -> {
          ArticlesUsers articlesUsers = getArticlesUsers(article, user);
          entityManager.persist(articlesUsers);
          return articlesUsers;
        });
  }

  private Comment createComment(User author, Article article, String body) {
    return transaction(
        () -> {
          Comment comment = new Comment();
          comment.setBody(body);
          comment.setArticle(article);
          comment.setAuthor(author);
          entityManager.persist(comment);
          return comment;
        });
  };

  private NewArticleDTO createNewArticle(
      String title, String description, String body, String... tagList) {
    NewArticleDTO newArticleDTO = new NewArticleDTO();
    newArticleDTO.setTitle(title);
    newArticleDTO.setDescription(description);
    newArticleDTO.setBody(body);
    newArticleDTO.setTagList(Arrays.asList(tagList));
    return newArticleDTO;
  }

  private List<Article> createArticles(
      User author, String title, String description, String body, int quantity) {
    return transaction(
        () -> {
          List<Article> articles = new LinkedList<>();

          for (int articleIndex = 0; articleIndex < quantity; articleIndex++) {
            articles.add(
                createArticle(
                    author,
                    title + "_" + articleIndex,
                    description + "_" + articleIndex,
                    body + "_" + articleIndex));
          }

          return articles;
        });
  }

  private Article createArticle(User author, String title, String description, String body) {
    return transaction(
        () -> {
          Article article =
              new ArticleBuilder()
                  .title(title)
                  .slug(slugify.slugify(title))
                  .description(description)
                  .body(body)
                  .author(author)
                  .build();
          entityManager.persist(article);
          return article;
        });
  }

  private void createArticles(
      User author,
      String title,
      String description,
      String body,
      int quantity,
      InsertResult<Article> insertResult) {

    transaction(
        () -> {
          for (int articleIndex = 0; articleIndex < quantity; articleIndex++) {

            Article article =
                new ArticleBuilder()
                    .title(title + "_" + articleIndex)
                    .description(description + "_" + articleIndex)
                    .body(body + "_" + articleIndex)
                    .build();
            int id = insertResult.add(article);
            article.setId((long) id);
            Date date = new Date();

            entityManager
                .createNativeQuery(
                    "INSERT INTO ARTICLES (ID, TITLE, DESCRIPTION, BODY, CREATEDAT, UPDATEDAT, AUTHOR_ID) VALUES (?, ?, ?, ?, ?, ?, ?)")
                .setParameter(1, id)
                .setParameter(2, article.getTitle())
                .setParameter(3, article.getDescription())
                .setParameter(4, article.getBody())
                .setParameter(5, date, TemporalType.TIMESTAMP)
                .setParameter(6, date, TemporalType.TIMESTAMP)
                .setParameter(7, author.getId())
                .executeUpdate();
          }
        });
  }

  private void follow(User currentUser, User... followers) {

    transaction(
        () -> {
          User user = entityManager.find(User.class, currentUser.getId());

          for (User follower : followers) {
            UsersFollowersKey key = new UsersFollowersKey();
            key.setUser(user);
            key.setFollower(follower);

            UsersFollowers usersFollowers = new UsersFollowers();
            usersFollowers.setPrimaryKey(key);
            entityManager.persist(usersFollowers);
          }

          entityManager.persist(user);
        });
  }

  private Tag createTag(String name) {
    return transaction(
        () -> {
          Tag tag = new Tag(name);
          entityManager.persist(tag);
          return tag;
        });
  }

  private List<ArticlesTags> createArticlesTags(List<Article> articles, Tag... tags) {
    return transaction(
        () -> {
          List<ArticlesTags> resultList = new LinkedList<>();

          for (Article article : articles) {

            Article managedArticle = entityManager.find(Article.class, article.getId());

            for (Tag tag : tags) {
              Tag managedTag = entityManager.find(Tag.class, tag.getId());

              ArticlesTagsKey articlesTagsKey = new ArticlesTagsKey();
              articlesTagsKey.setArticle(managedArticle);
              articlesTagsKey.setTag(managedTag);

              ArticlesTags articlesTags = new ArticlesTags();
              articlesTags.setPrimaryKey(articlesTagsKey);

              entityManager.persist(articlesTags);
              resultList.add(articlesTags);
            }
          }

          return resultList;
        });
  }

  private User createUser(
      String username, String email, String bio, String image, String password, Role... role) {
    return transaction(
        () -> {
          User user = UserUtils.create(username, email, password, bio, image);
          entityManager.persist(user);
          user.setToken(jwtService.sign(user.getId().toString(), role));
          entityManager.merge(user);
          return user;
        });
  }

  private ArticlesUsers getArticlesUsers(Article article, User loggedUser) {
    ArticlesUsersKey articlesUsersKey = getArticlesUsersKey(article, loggedUser);
    ArticlesUsers articlesUsers = new ArticlesUsers();
    articlesUsers.setPrimaryKey(articlesUsersKey);
    return articlesUsers;
  }

  private ArticlesUsersKey getArticlesUsersKey(Article article, User loggedUser) {
    ArticlesUsersKey articlesUsersKey = new ArticlesUsersKey();
    articlesUsersKey.setArticle(article);
    articlesUsersKey.setUser(loggedUser);
    return articlesUsersKey;
  }
}
