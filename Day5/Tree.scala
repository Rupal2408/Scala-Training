import scala.collection.mutable.ListBuffer
import scala.io.StdIn.readLine
import scala.language.implicitConversions

case class Employee(sno: Int, name: String, city: String)

// Method to implicitly convert tuple to Employee instance
implicit def tupleToEmployee(tuple: (Int, String, String)): Employee = Employee(tuple._1, tuple._2, tuple._3)

class DepartmentNode(val name: String) {
  var employees: ListBuffer[Employee] = ListBuffer()  
  var subDepartments: ListBuffer[DepartmentNode] = ListBuffer()  

  def addEmployee(employee: Employee): Unit = {
    employees += employee
  }

  def addSubDepartment(subDept: DepartmentNode): Unit = {
    subDepartments += subDept
  }

  def printDepartment(indent: String): Unit = {
    println(s"$indent└── $name")
    
    for (emp <- employees) {
      println(s"$indent    ├── (${emp.sno}, ${emp.name}, ${emp.city})")
    }

    for (subDept <- subDepartments) {
      subDept.printDepartment(indent + "    ")
    }
  }

  def findDepartment(name: String): Option[DepartmentNode] = {
    if (this.name == name) {
      Some(this)
    } 
    else {
      subDepartments.flatMap(_.findDepartment(name)).headOption
    }
  }
}

object OrganizationApplication {
  var rootDepartment: DepartmentNode = new DepartmentNode("Organization")

  def findOrCreateDepartment(parentDept: DepartmentNode, deptName: String): DepartmentNode = {
    parentDept.findDepartment(deptName) match {
      case Some(existingDept) => 
        existingDept  
      case None =>
        val newDept = new DepartmentNode(deptName)
        parentDept.addSubDepartment(newDept)
        newDept
    }
  }

  def FetchOrgData(): Unit = {
    var continue = true
    while (continue) {

      val input = readLine("Enter Parent Department and Current Department (comma separated) or type 'exit':\n")
      if (input == "exit") {
        continue = false
      } else {
        val data = input.split(",").map(_.trim)
        if (data.length == 2) {
          val parentDeptName = data(0)
          val currentDeptName = data(1)

          // Find or create the parent department
          val parentDept = findOrCreateDepartment(rootDepartment, parentDeptName)

          // Find or create the current department as a sub-department under the parent department
          val currentDept = findOrCreateDepartment(parentDept, currentDeptName)

          var execute = true
          while (execute) {
            println("Choose an option:")
            println("1. Save department details")
            println("2. Add employee details")

            val choice = readLine().trim()

            choice match {
              case "1" =>
                // Save department details
                println(s"Department '$currentDeptName' is saved under parent department '$parentDeptName'.")
                execute = false
              
              case "2" =>
                // Add employee details
                var addingEmployees = true
                while (addingEmployees) {
                  val employeeInput = readLine("Enter Employee details (sno, name, city):\n")

                  val employeeInfo = employeeInput.split(",").map(_.trim)
                  if (employeeInfo.length == 3) {
                    try {
                      val sno = employeeInfo(0).toInt
                      val name = employeeInfo(1)
                      val city = employeeInfo(2)

                      val employee: Employee = (sno, name, city)
                      currentDept.addEmployee(employee)
                      println(s"Employee ($sno, $name, $city) is added to department '$currentDeptName'.")
                      
                      val continueAdding = readLine("Add another employee? (y/n):\n").trim.toLowerCase
                      if (continueAdding != "y") {
                        addingEmployees = false
                      }
                    } catch {
                      case _: Exception => println("Invalid employee details. Please ensure correct format (sno, name, city).")
                    }
                  } else {
                    println("Invalid employee input format.")
                  }
                }
                execute = false

              case _ =>
                println("Invalid option. Please choose 1 or 2.")
            }
          }
        } else {
          println("Invalid input. Please enter the parent department and current department as two comma-separated values.")
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    FetchOrgData()

    // Print the organization structure
    println("\nOrganization Structure:")
    rootDepartment.printDepartment("")
  }
}



