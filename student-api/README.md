# Student API — Basic CRUD Demo

A minimal Spring Boot REST API for beginners: `Student` and `Teacher` entities with basic CRUD, an H2 in-memory database, a foreign-key relationship between them, and two simple `@Query` JOIN examples.

## Project Structure

```
student-api/
├── pom.xml
└── src/main/
    ├── java/com/example/studentapi/
    │   ├── StudentApiApplication.java        (main class)
    │   ├── model/
    │   │   ├── Student.java                  (JPA entity, has teacher_id FK)
    │   │   └── Teacher.java                  (JPA entity)
    │   ├── repository/
    │   │   ├── StudentRepository.java        (JPA repo + @Query joins)
    │   │   └── TeacherRepository.java        (JPA repo)
    │   └── controller/
    │       ├── StudentController.java        (student REST endpoints)
    │       └── TeacherController.java        (teacher REST endpoints)
    └── resources/application.properties      (H2 + server config)
```

## The Relationship: Student → Teacher

One teacher can have many students. `Student` carries the foreign key:

```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "teacher_id")
private Teacher teacher;
```

- `@ManyToOne` — many students can point to the same teacher.
- `@JoinColumn(name = "teacher_id")` — the `student` table gets a `teacher_id` column holding the teacher's id (the FK).

H2 creates this automatically (see `ddl-auto=create-drop` in `application.properties`). Open the H2 console and run `SELECT * FROM STUDENT` — you'll see the `TEACHER_ID` column.

## API Endpoints

### Teachers

| Method | Endpoint             | Description       |
|--------|----------------------|-------------------|
| GET    | `/api/teachers`      | Get all teachers  |
| GET    | `/api/teachers/{id}` | Get teacher by id |
| POST   | `/api/teachers`      | Create a teacher  |
| PUT    | `/api/teachers/{id}` | Update a teacher  |
| DELETE | `/api/teachers/{id}` | Delete a teacher  |

### Students

| Method | Endpoint                              | Description                    |
|--------|---------------------------------------|--------------------------------|
| GET    | `/api/students`                       | Get all students               |
| GET    | `/api/students/{id}`                  | Get student by id              |
| POST   | `/api/students`                       | Create a student               |
| PUT    | `/api/students/{id}`                  | Update a student               |
| DELETE | `/api/students/{id}`                  | Delete a student               |
| GET    | `/api/students/by-teacher?name=`      | JOIN query: students of a teacher |
| GET    | `/api/students/teacher-counts`        | JOIN + GROUP BY: count per teacher |

## How to Run

```bash
cd student-api
mvn spring-boot:run
```

App starts on `http://localhost:8080`. Stop with `Ctrl+C`.

H2 console: `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:studentdb`, username `sa`, no password. Great for showing students the actual tables Hibernate created.

## Postman Walkthrough (the classroom demo)

1. **Create a teacher**
   `POST http://localhost:8080/api/teachers`
   ```json
   { "name": "Mrs. Sharma", "subject": "Math" }
   ```
   → returns `{ "id": 1, "name": "Mrs. Sharma", "subject": "Math" }`

2. **Create a student linked to that teacher**
   `POST http://localhost:8080/api/students`
   ```json
   {
     "name": "Alice",
     "email": "alice@example.com",
     "age": 21,
     "teacher": { "id": 1 }
   }
   ```
   Just passing `{ "id": 1 }` is enough — the controller looks up that teacher, sets it on the student, and saves. The response shows the full nested teacher object.

3. **Create one more student under the same teacher**, and optionally a second teacher + student.

4. **GET /api/students** → each student now includes their teacher nested inside:
   ```json
   {
     "id": 1,
     "name": "Alice",
     "email": "alice@example.com",
     "age": 21,
     "teacher": { "id": 1, "name": "Mrs. Sharma", "subject": "Math" }
   }
   ```

5. **GET /api/students/by-teacher?name=Mrs. Sharma** → only her students.

6. **GET /api/students/teacher-counts** → `[["Mrs. Sharma", 2], ...]`

## The JOIN Queries (in StudentRepository.java)

```java
@Query("SELECT s FROM Student s JOIN s.teacher t WHERE t.name = :teacherName")
List<Student> findStudentsByTeacherName(@Param("teacherName") String teacherName);

@Query("SELECT t.name, COUNT(s) FROM Student s JOIN s.teacher t GROUP BY t.name")
List<Object[]> countStudentsPerTeacher();
```

Things to point out to students:

- This is **JPQL**, not SQL — it queries *entities and their fields* (`Student`, `s.teacher`), not tables and columns. JPA translates it to real SQL with a real `JOIN`.
- `s.teacher` — you "walk" the relationship with a dot, like a normal Java object. No manual `ON student.teacher_id = teacher.id` needed.
- `:teacherName` is a named parameter; `@Param("teacherName")` connects it to the method argument. Never build JPQL with string concatenation.
- `spring.jpa.show-sql=true` is already on — watch the console while hitting the endpoints and show students the generated `SELECT ... FROM student JOIN teacher ...` SQL.
- `List<Object[]>` — when a query returns more than one column (`name` + `count`), each row comes back as an `Object[]`.

## Data Notes

- H2 is **in-memory** — every restart wipes the data. Perfect for demos; just recreate a teacher and students each run.
- `ddl-auto=create-drop` — Hibernate creates tables on startup, drops on shutdown. Mention that real projects use `validate` + Flyway/Liquibase instead.
