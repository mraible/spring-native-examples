package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Post;
import com.mycompany.myapp.repository.EntityManager;
import com.mycompany.myapp.repository.PostRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link PostResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class PostResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/posts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PostRepository postRepository;

    @Mock
    private PostRepository postRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Post post;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Post createEntity(EntityManager em) {
        Post post = new Post().title(DEFAULT_TITLE).content(DEFAULT_CONTENT).date(DEFAULT_DATE);
        return post;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Post createUpdatedEntity(EntityManager em) {
        Post post = new Post().title(UPDATED_TITLE).content(UPDATED_CONTENT).date(UPDATED_DATE);
        return post;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll("rel_post__tag").block();
            em.deleteAll(Post.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        post = createEntity(em);
    }

    @Test
    void createPost() throws Exception {
        int databaseSizeBeforeCreate = postRepository.findAll().collectList().block().size();
        // Create the Post
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeCreate + 1);
        Post testPost = postList.get(postList.size() - 1);
        assertThat(testPost.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPost.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testPost.getDate()).isEqualTo(DEFAULT_DATE);
    }

    @Test
    void createPostWithExistingId() throws Exception {
        // Create the Post with an existing ID
        post.setId(1L);

        int databaseSizeBeforeCreate = postRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = postRepository.findAll().collectList().block().size();
        // set the field null
        post.setTitle(null);

        // Create the Post, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = postRepository.findAll().collectList().block().size();
        // set the field null
        post.setDate(null);

        // Create the Post, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllPosts() {
        // Initialize the database
        postRepository.save(post).block();

        // Get all the postList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(post.getId().intValue()))
            .jsonPath("$.[*].title")
            .value(hasItem(DEFAULT_TITLE))
            .jsonPath("$.[*].content")
            .value(hasItem(DEFAULT_CONTENT.toString()))
            .jsonPath("$.[*].date")
            .value(hasItem(DEFAULT_DATE.toString()));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPostsWithEagerRelationshipsIsEnabled() {
        when(postRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(postRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPostsWithEagerRelationshipsIsNotEnabled() {
        when(postRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(postRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getPost() {
        // Initialize the database
        postRepository.save(post).block();

        // Get the post
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, post.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(post.getId().intValue()))
            .jsonPath("$.title")
            .value(is(DEFAULT_TITLE))
            .jsonPath("$.content")
            .value(is(DEFAULT_CONTENT.toString()))
            .jsonPath("$.date")
            .value(is(DEFAULT_DATE.toString()));
    }

    @Test
    void getNonExistingPost() {
        // Get the post
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewPost() throws Exception {
        // Initialize the database
        postRepository.save(post).block();

        int databaseSizeBeforeUpdate = postRepository.findAll().collectList().block().size();

        // Update the post
        Post updatedPost = postRepository.findById(post.getId()).block();
        updatedPost.title(UPDATED_TITLE).content(UPDATED_CONTENT).date(UPDATED_DATE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedPost.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedPost))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
        Post testPost = postList.get(postList.size() - 1);
        assertThat(testPost.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPost.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testPost.getDate()).isEqualTo(UPDATED_DATE);
    }

    @Test
    void putNonExistingPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().collectList().block().size();
        post.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, post.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().collectList().block().size();
        post.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().collectList().block().size();
        post.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdatePostWithPatch() throws Exception {
        // Initialize the database
        postRepository.save(post).block();

        int databaseSizeBeforeUpdate = postRepository.findAll().collectList().block().size();

        // Update the post using partial update
        Post partialUpdatedPost = new Post();
        partialUpdatedPost.setId(post.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPost.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPost))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
        Post testPost = postList.get(postList.size() - 1);
        assertThat(testPost.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPost.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testPost.getDate()).isEqualTo(DEFAULT_DATE);
    }

    @Test
    void fullUpdatePostWithPatch() throws Exception {
        // Initialize the database
        postRepository.save(post).block();

        int databaseSizeBeforeUpdate = postRepository.findAll().collectList().block().size();

        // Update the post using partial update
        Post partialUpdatedPost = new Post();
        partialUpdatedPost.setId(post.getId());

        partialUpdatedPost.title(UPDATED_TITLE).content(UPDATED_CONTENT).date(UPDATED_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPost.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPost))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
        Post testPost = postList.get(postList.size() - 1);
        assertThat(testPost.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPost.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testPost.getDate()).isEqualTo(UPDATED_DATE);
    }

    @Test
    void patchNonExistingPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().collectList().block().size();
        post.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, post.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().collectList().block().size();
        post.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().collectList().block().size();
        post.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(post))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deletePost() {
        // Initialize the database
        postRepository.save(post).block();

        int databaseSizeBeforeDelete = postRepository.findAll().collectList().block().size();

        // Delete the post
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, post.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Post> postList = postRepository.findAll().collectList().block();
        assertThat(postList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
