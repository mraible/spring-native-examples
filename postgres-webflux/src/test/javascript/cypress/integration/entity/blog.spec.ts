import { entityItemSelector } from '../../support/commands';
import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityCreateCancelButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('Blog e2e test', () => {
  const blogPageUrl = '/blog';
  const blogPageUrlPattern = new RegExp('/blog(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'admin';
  const password = Cypress.env('E2E_PASSWORD') ?? 'admin';
  const blogSample = { name: 'Handmade next-generation 1080p', handle: 'hack wireless' };

  let blog: any;

  beforeEach(() => {
    cy.getOauth2Data();
    cy.get('@oauth2Data').then(oauth2Data => {
      cy.oauthLogin(oauth2Data, username, password);
    });
    cy.intercept('GET', '/api/blogs').as('entitiesRequest');
    cy.visit('');
    cy.get(entityItemSelector).should('exist');
  });

  beforeEach(() => {
    Cypress.Cookies.preserveOnce('XSRF-TOKEN', 'JSESSIONID');
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/blogs+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/blogs').as('postEntityRequest');
    cy.intercept('DELETE', '/api/blogs/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (blog) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/blogs/${blog.id}`,
      }).then(() => {
        blog = undefined;
      });
    }
  });

  afterEach(() => {
    cy.oauthLogout();
    cy.clearCache();
  });

  it('Blogs menu should load Blogs page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('blog');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response!.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Blog').should('exist');
    cy.url().should('match', blogPageUrlPattern);
  });

  describe('Blog page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(blogPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Blog page', () => {
        cy.get(entityCreateButtonSelector).click({ force: true });
        cy.url().should('match', new RegExp('/blog/new$'));
        cy.getEntityCreateUpdateHeading('Blog');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click({ force: true });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response!.statusCode).to.equal(200);
        });
        cy.url().should('match', blogPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/blogs',
          body: blogSample,
        }).then(({ body }) => {
          blog = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/blogs+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [blog],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(blogPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Blog page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('blog');
        cy.get(entityDetailsBackButtonSelector).click({ force: true });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response!.statusCode).to.equal(200);
        });
        cy.url().should('match', blogPageUrlPattern);
      });

      it('edit button click should load edit Blog page', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Blog');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click({ force: true });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response!.statusCode).to.equal(200);
        });
        cy.url().should('match', blogPageUrlPattern);
      });

      it('last delete button click should delete instance of Blog', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('blog').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click({ force: true });
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response!.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response!.statusCode).to.equal(200);
        });
        cy.url().should('match', blogPageUrlPattern);

        blog = undefined;
      });
    });
  });

  describe('new Blog page', () => {
    beforeEach(() => {
      cy.visit(`${blogPageUrl}`);
      cy.get(entityCreateButtonSelector).click({ force: true });
      cy.getEntityCreateUpdateHeading('Blog');
    });

    it('should create an instance of Blog', () => {
      cy.get(`[data-cy="name"]`).type('SAS').should('have.value', 'SAS');

      cy.get(`[data-cy="handle"]`).type('Avon').should('have.value', 'Avon');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response!.statusCode).to.equal(201);
        blog = response!.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response!.statusCode).to.equal(200);
      });
      cy.url().should('match', blogPageUrlPattern);
    });
  });
});
