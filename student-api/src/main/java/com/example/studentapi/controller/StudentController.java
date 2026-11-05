package com.example.studentapi.controller;

import com.example.studentapi.model.Student;
import com.example.studentapi.model.Teacher;
import com.example.studentapi.repository.StudentRepository;
import com.example.studentapi.repository.TeacherRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public StudentController(StudentRepository studentRepository,
                             TeacherRepository teacherRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @GetMapping("/getById")
    public Student getStudentById(@RequestParam  Long id) {
        return studentRepository.findById(id).get();
    }

    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        if (student.getTeacher() != null) {
            Teacher teacher = teacherRepository.findById(student.getTeacher().getId()).get();
            student.setTeacher(teacher);
        }
        return studentRepository.save(student);
    }

    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable Long id, @RequestBody Student student) {
        Student existing = studentRepository.findById(id).get();
        existing.setName(student.getName());
        existing.setEmail(student.getEmail());
        existing.setAge(student.getAge());
        if (student.getTeacher() != null) {
            Teacher teacher = teacherRepository.findById(student.getTeacher().getId()).get();
            existing.setTeacher(teacher);
        }
        return studentRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentRepository.deleteById(id);
        return "Student deleted with id: " + id;
    }

    @GetMapping("/by-teacher")
    public List<Student> getStudentsByTeacherName(@RequestParam String name) {
        return studentRepository.findStudentsByTeacherName(name);
    }

    @GetMapping("/teacher-counts")
    public List<Object[]> countStudentsPerTeacher() {
        return studentRepository.countStudentsPerTeacher();
    }
}
