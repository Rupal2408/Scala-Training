/* TASK
1. Create case class Student (sno, name, score)
2. Process a list of students:
   a) Student list % > 70
   b) Student list % < 60
3. Print the student list
4. Add a method for implicit conversion from tuple to Student to add a student
5. A method which takes array of students as input and returns filtered list based on any any condition which has boolean return type.
*/

import scala.collection.mutable.ListBuffer

case class Student(sno: Int, name: String, score: Int)

implicit class processStudentList(student: List[Student]) {
    def %>(cutoff: Int): List[Student] = {
        student.filter(_.score>cutoff)
    }

    def %<(cutoff: Int): List[Student] = {
        student.filter(_.score<cutoff)
    }
}

implicit class processStudentArray(students: Array[Student]){
    def filterRecords(student: Student => Boolean) = {
    students.filter(student)
}
}

implicit def tupleToStudent(student: (Int,String,Int)): Student = {
  Student(student._1, student._2, student._3)
}

def printList(students:List[Student]) = {
    students.foreach(student => {
    println(s"${student.name} has scored ${student.score}%.")
    })
}

def printArray(students:Array[Student]) = {
    students.foreach(student => {
    println(s"${student.name} has scored ${student.score}%.")
    })
}

@main  def Runit = {
// Usage
val studentList = ListBuffer(Student(1, "James", 90), Student(2, "Brian", 50), Student(3, "Rachel", 80)) 
studentList += ((4,"Monica", 45))
val students = studentList.toList
val studentsGreaterThanCutoff = students %> 70
val studentsLessThanCutoff = students %< 60
println("List of Student with percentage greater than 70% : ")
printList(studentsGreaterThanCutoff)
println()
println("List of Student with percentage less than 60% : ")
printList(studentsLessThanCutoff)

val studentArray = Array(Student(1, "James", 90), Student(2, "Brian", 50), Student(3, "Rachel", 68))
println()
println("Array of students with percentage between 60-75:")
printArray(studentArray.filterRecords(st => (st.score > 60 && st.score < 75)))
println()
println("Array of students with with A Grade:")
printArray(studentArray.filterRecords(st => st.score >= 90))
}