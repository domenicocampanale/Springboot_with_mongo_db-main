describe('Movie E2E Tests', () => {
    const baseUrl = 'http://localhost:8080/view/movie/';
    let movieId;

    before(() => {
        // Pulisce il database prima di iniziare
        cy.request('GET', baseUrl + 'reset');
    });

    it('Test Home Page', () => {
        cy.visit(baseUrl + 'home');
        cy.title().should('eq', 'Movie Home Page');
    });

    it('Test Add Movie', () => {
        cy.visit(baseUrl + 'add');
        cy.get('button').click();
        cy.url().should('include', 'list?success');
    });

    it('Test Movie List', () => {
        cy.visit(baseUrl + 'list');
        cy.get('li').first().find('p').contains('Movie ID:').invoke('text').then(text => {
            movieId = text.split(': ')[1];
        });
    });

    it('Test Edit Movie', () => {
        cy.visit(`${baseUrl}edit?id=${movieId}`);
        cy.get('#title').clear().type('Film Modificato Cypress');
        cy.get('button').click();
        cy.url().should('include', 'list?success');
    });

    it('Test Movie Found', () => {
        cy.visit(`${baseUrl}details?id=${movieId}`);
        cy.contains('p', 'Title:').should('be.visible');
        cy.contains('p', 'Release Date:').should('be.visible');
    });

    it('Test Movie Not Found', () => {
        cy.visit(`${baseUrl}details?id=999`);
        cy.get('.error').should('be.visible');
    });

    it('Test Edit Movie Not Found', () => {
        cy.visit(`${baseUrl}edit?id=999`);
        cy.get('.error').should('be.visible');
    });

    it('Test Delete Movie Not Found', () => {
        cy.visit(`${baseUrl}delete?id=999`);
        cy.get('.error').should('be.visible');
    });

    it('Test Add Movie Client Validation', () => {
        cy.visit(baseUrl + 'add');
        cy.get('#title').clear();
        cy.get('button').click();
        cy.url().should('eq', baseUrl + 'add');
    });

    it('Test Add Movie With Invalid Data', () => {
        cy.visit(baseUrl + 'add');
        cy.get('#title').type('Test');
        cy.get('#releaseDate').type('invalid-date');
        cy.get('button').click();
        cy.get("div.error").should('be.visible');
    });

    it('Test Edit Movie With Invalid Data', () => {
        cy.visit(`${baseUrl}edit?id=${movieId}`);
        cy.get('#releaseDate').clear().type('invalid-date');
        cy.get('button').click();
        cy.get("div.error").should('be.visible');
    });

    it('Test Patch Movie', () => {
        // Visita la pagina Patch Movie
        cy.visit(`${baseUrl}patch?id=${movieId}`);

        // Seleziona il campo "Title" nel select
        cy.get('#fieldToUpdate').select('title');

        // Assicurati che l'input "New Value" sia di tipo testo (dovrebbe essere il comportamento predefinito)
        cy.get('#updateValue').should('have.attr', 'type', 'text');

        // Scrivi il nuovo valore nel campo "New Value"
        cy.get('#updateValue').clear().type('Film Aggiornato con PATCH');

        // Clicca sul bottone per inviare il modulo
        cy.get('button').click();

        // Verifica che l'URL contenga "list?success"
        cy.url().should('include', 'list?success');

        // Visita la pagina dei dettagli del film per verificare che il titolo sia stato aggiornato
        cy.visit(`${baseUrl}details?id=${movieId}`);

        // Verifica che il titolo del film sia stato aggiornato
        cy.contains('p', 'Title:').should('contain', 'Film Aggiornato con PATCH');
    });


    it('Test Patch Movie Client Validation', () => {
        cy.visit(`${baseUrl}patch?id=${movieId}`);
        cy.get('#updateValue').clear();
        cy.get('button').click();
        cy.url().should('eq', `${baseUrl}patch?id=${movieId}`);
    });

    it('Test Patch Movie Not Found', () => {
        cy.visit(`${baseUrl}patch?id=999`);
        cy.get('.error').should('be.visible');
    });

    it('Test Delete Movie', () => {
        cy.visit(`${baseUrl}delete?id=${movieId}`);
        cy.url().should('include', 'list?success');
    });
});
