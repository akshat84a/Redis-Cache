package com.example.redis.cache.controller;

import com.example.redis.cache.DTO.EmployeeDTO;
import com.example.redis.cache.entity.Employee;
import com.example.redis.cache.repository.EmployeeRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeController {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    ModelMapper modelMapper;
    @PostMapping("/employees")
    public Employee addEmployee(@RequestBody  EmployeeDTO dto){
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        Employee employee = modelMapper.map(dto,Employee.class);
        return employeeRepository.save(employee);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeRepository.findAll());
    }

    @GetMapping("/employees/{employeeId}")
    @Cacheable(value = "employees", key="#employeeId")
    public Employee findEmployeeById(@PathVariable Integer employeeId) throws Exception {
        System.out.println("Employee fetching from database:: "+employeeId);
        return employeeRepository.findById(employeeId).orElseThrow(
                () -> new Exception("Employee not found" + employeeId));
    }

    @PutMapping("employees/{employeeId}")
    @CachePut(value = "employees",key = "#employeeId")
    public Employee updateEmployee(@PathVariable(value = "employeeId") Integer employeeId,
                                   @RequestBody Employee employeeDetails) throws Exception {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new Exception("Employee not found for this id :: " + employeeId));
        employee.setName(employeeDetails.getName());
        final Employee updatedEmployee = employeeRepository.save(employee);
        return updatedEmployee;

    }


    @DeleteMapping("employees/{id}")
    @CacheEvict(value = "employees", allEntries = true)
    public void deleteEmployee(@PathVariable(value = "id") Integer employeeId) throws Exception {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(
                () -> new Exception("Employee not found" + employeeId));
        employeeRepository.delete(employee);
    }
}
