package com.zjh.ssh.service;

import java.util.List;

import com.zjh.ssh.dao.DepartmentDao;
import com.zjh.ssh.entities.Department;

public class DepartmentService {
	
	private DepartmentDao departmentDao;
	
	public void setDepartmentDao(DepartmentDao departmentDao) {
		this.departmentDao = departmentDao;
	}
	
	public List<Department> getAll(){
		return departmentDao.getAll();
	}
	
}
