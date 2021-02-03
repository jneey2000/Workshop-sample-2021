package page.chungjungsoo.to_dosample.todo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import page.chungjungsoo.to_dosample.R
import java.util.*

// 할일의 기한 due date setting

class TodoListViewAdapter (context: Context, var resource: Int, var items: MutableList<Todo> ) : ArrayAdapter<Todo>(context, resource, items){
    private lateinit var db: TodoDatabaseHelper

    override fun getView(position: Int, convertView: View?, p2: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = layoutInflater.inflate(resource , null )
        val title : TextView = view.findViewById(R.id.listTitle)
        val description : TextView = view.findViewById(R.id.listDesciption)
        val edit : Button = view.findViewById(R.id.editBtn)
        val delete : Button = view.findViewById(R.id.delBtn)

        db = TodoDatabaseHelper(this.context)

        // Get to-do item
        var todo = items[position]

        // Load title and description to single ListView item
        title.text = todo.title
        description.text = todo.description

        // OnClick Listener for edit button on every ListView items
        edit.setOnClickListener {
            // Very similar to the code in MainActivity.kt
            val builder = AlertDialog.Builder(this.context)
            val dialogView = layoutInflater.inflate(R.layout.add_todo_dialog, null)
            val titleToAdd = dialogView.findViewById<EditText>(R.id.todoTitle)
            val desciptionToAdd = dialogView.findViewById<EditText>(R.id.todoDescription)
            val dueDateToAdd = dialogView.findViewById<Button>(R.id.dueDate) // dueDate - button - 창 / 거기서 날짜를 고른 그 값을 datetext에 넣어줌, datetext를 출력
            val datetextToAdd = dialogView.findViewById<TextView>(R.id.dateText) // 편집 부분에도 할 준비가 됨!
            val finishedToAdd = dialogView.findViewById<CheckBox>(R.id.finishedToAdd)
            val ime = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


            titleToAdd.setText(todo.title) //todo - db 47 ~
            desciptionToAdd.setText(todo.description)
            // dueDate 창을 뜨게하는 버튼 (dueDate는 사실 db에 없어도 됨. dateText만 db에 있으면 됨)
            datetextToAdd.setText(todo.date) // db에 있는 date값을 dateTextToAdd 변수에 넣어줌
            finishedToAdd.isChecked = todo.finished


            titleToAdd.requestFocus()
            ime.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            // 밑에 있는 거는 59 ~ 71 duedate button 기능
            dueDateToAdd.setOnClickListener{ // 파라미터 안씀 / ischecked 는 행동, 날짜는 행동이 아니라 정해진 것을 가져옴 그래서 다름
                ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0) //datepickerdialog 라이브러리 x
                val today = GregorianCalendar()
                val year: Int = today.get(Calendar.YEAR)
                val month: Int = today.get(Calendar.MONTH)
                val date: Int = today.get(Calendar.DATE)

                val dlg = DatePickerDialog(this.context, DatePickerDialog.OnDateSetListener //this 화면 안에 들어갈거다! / edit : add랑 달라서 context? )
                { view, year, month, dayOfMonth -> datetextToAdd.text = "${year}년 ${month+1}월 ${dayOfMonth}일" }, year, month, date)
                dlg.show() // datetextToAdd 레이아웃 추가


            } // button 기능 함수 (70부터 여기까지)
            //밑에는 finished기능 72 ~ 74
            finishedToAdd.setOnCheckedChangeListener{_, ischecked -> // true,false, 함수임
                finishedToAdd.isChecked = ischecked //isChecked 원래 있는 거 , 행동하면 선언한 ischecked에 들어감 t,f 값을 레이아웃에 전달
            }
            // add _ 처음부터 추가 edit_db에있는거 가져옴 (과정은 하나 차이)  차이점은 db

            builder.setView(dialogView)
                .setPositiveButton("수정") { _, _ ->
                    val tmp = Todo(
                        titleToAdd.text.toString(),
                        desciptionToAdd.text.toString(),
                        datetextToAdd.text.toString(),
                        finishedToAdd.isChecked // 1.title 2.description 3.date 4.finished
                    )

                    val result = db.updateTodo(tmp, position)
                    if (result) {
                        todo.title = titleToAdd.text.toString()
                        todo.description = desciptionToAdd.text.toString()
                        todo.date = datetextToAdd.text.toString() // 수정사항 toSting() 는 datetextToAdd에 날짜값을 todotable에 넣어줌
                        todo.finished = finishedToAdd.isChecked // isChecked는 체크박스 고유 변수
                        notifyDataSetChanged()
                        ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)

                    }
                    else {
                        Toast.makeText(this.context, "수정 실패! :(", Toast.LENGTH_SHORT).show()
                        notifyDataSetChanged()
                    }
                }
                .setNegativeButton("취소") {_, _ ->
                    // Cancel Btn. Do nothing. Close keyboard.
                    ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                }
                .show()
        }

        // OnClick Listener for X(delete) button on every ListView items
        delete.setOnClickListener {
            val result = db.delTodo(position)
            if (result) {
                items.removeAt(position)
                notifyDataSetChanged()
            }
            else {
                Toast.makeText(this.context, "삭제 실패! :(", Toast.LENGTH_SHORT).show()
                notifyDataSetChanged()
            }
        }

        return view
    }
}