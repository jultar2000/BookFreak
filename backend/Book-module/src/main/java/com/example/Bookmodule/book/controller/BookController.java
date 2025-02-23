package com.example.Bookmodule.book.controller;

import com.example.Bookmodule.author.entity.Author;
import com.example.Bookmodule.author.service.AuthorService;
import com.example.Bookmodule.book.dto.*;
import com.example.Bookmodule.book.entity.Book;
import com.example.Bookmodule.book.event.OrderModuleEventClient;
import com.example.Bookmodule.book.service.BookService;
import com.example.Bookmodule.book.service.CommentService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@AllArgsConstructor
public class BookController {

    private final BookService bookService;

    private final CommentService commentService;

    private final AuthorService authorService;

    private final ModelMapper mapper;

    private final OrderModuleEventClient orderModuleEventClient;

    @GetMapping("/all")
    public ResponseEntity<List<GetBookDto>> getAllBooks() {
        return ResponseEntity.ok(bookService.findAllBooks());
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<GetBookDto> getBookById(@PathVariable("bookId") String bookId) {
        Book book = bookService.findBook(bookId);
        return ResponseEntity.ok(mapper.map(book, GetBookDto.class));
    }

    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<List<GetBookDto>> getBooksByKeyword(@PathVariable("keyword") String keyword) {
        return ResponseEntity.ok(bookService.findBooksByKeyword(keyword));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<GetBookDto>> getBooksByGenre(@PathVariable("genre") String genre) {
        return ResponseEntity.ok(bookService.findBooksByGenre(genre));
    }

    @GetMapping("/rating")
    public ResponseEntity<List<GetBookDto>> getBooksByRating() {
        return ResponseEntity.ok(bookService.findBooksByRating());
    }

    @GetMapping("/authors/{authorId}")
    public ResponseEntity<List<GetBookDto>> getBooksByAuthor(@PathVariable("authorId") String authorId) {
        Author author = authorService.findAuthor(authorId);
        if (author == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookService.findBooksByAuthor(author));
    }

    @PostMapping("/authors/{authorId}")
    public ResponseEntity<Void> addBook(@PathVariable("authorId") String authorId,
                                        @RequestBody CreateBookDto request) {
        Author author = authorService.findAuthor(authorId);
        if (author == null) {
            return ResponseEntity.notFound().build();
        }
        Book book = mapper.map(request, Book.class);
        if (!bookService.insertBook(book, author)) {
            return ResponseEntity.badRequest().build();
        }
        orderModuleEventClient.insertBook(book.getOid(), request.getPrice());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{bookId}/rating")
    public ResponseEntity<Void> updateRating(@PathVariable("bookId") String bookId,
                                             @RequestBody RatingRequest request) {
        if (!bookService.updateRating(bookId, request.getRating())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<Void> updateBook(@PathVariable("bookId") String bookId,
                                           @RequestBody UpdateBookDto request) {
        if (!bookService.updateBook(
                bookId,
                request.getNumberOfPages(),
                request.getDescription(),
                request.getGenre(),
                request.getPrice())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable("bookId") String bookId) {
        if (!bookService.deleteBook(bookId)) {
            return ResponseEntity.notFound().build();
        }
        orderModuleEventClient.deleteBook(bookId);
        commentService.deleteAllBookComments(bookId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{bookId}/comments")
    public ResponseEntity<List<GetCommentsDto>> getBookComments(@PathVariable("bookId") String bookId) {
        if (bookService.findBook(bookId) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(commentService.findBookComments(bookId));
    }

    /*
    >>TODO figure out a  better way to get a principal
     */
    @PostMapping("/{bookId}/comments")
    public ResponseEntity<Void> addComment(@PathVariable("bookId") String bookId,
                                           @RequestBody CommentDto request) {
        if (bookService.findBook(bookId) == null) {
            return ResponseEntity.notFound().build();
        }
        if (!commentService.insertComment(bookId, request.getUsername(), request.getText())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable("commentId") String commentId,
                                              @RequestBody CommentDto request) {
        if (!commentService.updateComment(commentId, request.getUsername(), request.getText())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{commentId}/users/{username}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") String commentId,
                                              @PathVariable("username") String username) {
        if (!commentService.deleteComment(commentId, username)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
