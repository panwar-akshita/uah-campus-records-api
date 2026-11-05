package com.example.studentapi.repository;

import com.example.studentapi.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s JOIN s.teacher t WHERE t.name = :teacherName")
    List<Student> findStudentsByTeacherName(@Param("teacherName") String teacherName);

    @Query("SELECT t.name, COUNT(s) FROM Student s JOIN s.teacher t GROUP BY t.name")
    List<Object[]> countStudentsPerTeacher();

    Student findStudentById(Long id);

    Student findStudentByNameAndAge(String name, int age);

    @Query("SELECT s FROM Student s WHERE s.id = :id and s.teacher = :includeTeacher")
    Student findStudentById(@Param("id") Long id, @Param("includeTeacher") boolean includeTeacher);


    
    
}
