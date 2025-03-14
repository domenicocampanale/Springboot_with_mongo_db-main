describe('ReviewE2ETest', () => {
    const baseUrlReview = 'http://localhost:8080/view/review/';
    const baseUrlMovie = 'http://localhost:8080/view/movie/';
    let reviewId;
    let movieId;

    before(() => {
        // Reset del database per i film e le recensioni
        cy.request('GET',baseUrlMovie + 'reset'); // Pulisce il database dei film
        cy.request('GET',baseUrlReview + 'reset'); // Pulisce il database delle recensioni
    });

    it('1. Test Home Page', () => {
        cy.visit(baseUrlReview + 'home');
        cy.title().should('eq', 'Reviews Home Page');
    });

    it('2. Test Add Movie and Review', () => {
        // Aggiungi un film
        cy.visit(baseUrlMovie + 'add');
        cy.get('button').click();

        // Estrai l'ID del film appena creato
        cy.visit(baseUrlMovie + 'list');
        cy.url().should('include', 'list');

        cy.get('li').first().then((movieItem) => {
            const text = movieItem.find('p:contains("Movie ID:")').text();
            movieId = text.split(": ")[1]?.trim(); // Estrai e pulisci il movieId
            cy.log('movieId: ' + movieId);
            expect(movieId).to.not.be.undefined;
        }).then(() => {
            // Dopo aver ottenuto movieId, aggiungi la recensione
            cy.visit(baseUrlReview + 'add');
            cy.get('#movieId').should('be.visible').clear().type(movieId);
            cy.get('button').click();
            cy.url().should('include', 'list');
        });
    });


    it('3. Test Review List', () => {
        cy.visit(baseUrlReview + 'list');
        cy.get('li').first().then((reviewListItem) => {
            reviewId = reviewListItem.find('p:contains("Review ID:")').text().split(": ")[1];
        });
    });

    it('4. Test Edit Review', () => {
        cy.visit(`${baseUrlReview}edit?id=${reviewId}`);
        cy.get('#comment').clear().type('Commento Modificato Cypress');
        cy.get('button').click();
        cy.url().should('include', 'list?success');
    });

    it('5. Test Review Found', () => {
        cy.visit(`${baseUrlReview}details?id=${reviewId}`);
        cy.contains('p', 'Comment:').should('be.visible');
        cy.contains('p', 'Rating:').should('be.visible');
    });

    it('6. Test Review Not Found', () => {
        cy.visit(`${baseUrlReview}details?id=999`);
        cy.get('.error').should('be.visible');
    });

    it('7. Test Edit Review Not Found', () => {
        cy.visit(`${baseUrlReview}edit?id=999`);
        cy.get('.error').should('be.visible');
    });

    it('8. Test Delete Review Not Found', () => {
        cy.visit(`${baseUrlReview}delete?id=999`);
        cy.get('.error').should('be.visible');
    });

    it('9. Test Add Review Client Validation', () => {
        cy.visit(baseUrlReview + 'add');
        cy.get('#comment').clear();
        cy.get('button').click();
        cy.url().should('eq', baseUrlReview + 'add');
    });

    it('10. Test Add Review with Invalid Data', () => {
        cy.visit(baseUrlMovie + 'list');
        cy.get('li').first().then((movieItems) => {
            movieId = movieItems.first().find('p:contains("Movie ID:")').text().split(": ")[1].trim();
        });

        cy.visit(baseUrlReview + 'add');
        cy.get('#movieId').should('be.visible').clear().type(movieId);
        cy.get('#rating').should('be.visible').clear().type('100');
        cy.get('button').click();
        cy.get('div.error').should('be.visible').contains('The rating must be at most 5');
    });

    it('11. Test Edit Review with Invalid Data', () => {
        cy.visit(`${baseUrlReview}edit?id=${reviewId}`);
        cy.get('#rating').should('be.visible').clear().type('100');
        cy.get('button').click();
        cy.get('div.error').should('be.visible').contains('The rating must be at most 5');
    });

    it('12. Test Delete Review', () => {
        cy.visit(`${baseUrlReview}delete?id=${reviewId}`);
        cy.url().should('include', 'list?success');
    });
});
