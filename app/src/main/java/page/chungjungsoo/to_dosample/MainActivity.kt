package page.chungjungsoo.to_dosample

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import page.chungjungsoo.to_dosample.todo.Todo
import page.chungjungsoo.to_dosample.todo.TodoDatabaseHelper
import page.chungjungsoo.to_dosample.todo.TodoListViewAdapter
import java.util.*

class MainActivity : AppCompatActivity() {
    var dbHandler : TodoDatabaseHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set content view - loads activity_main.xml
        setContentView(R.layout.activity_main)

        // Set app status bar color : white, force light status bar mode
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // Set light status bar mode depending on the android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController!!.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        }
        else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Add database helper and load data from database
        dbHandler = TodoDatabaseHelper(this)
        var todolist: MutableList<Todo> = dbHandler!!.getAll()

        // Put data with custom listview adapter
        todoList.adapter = TodoListViewAdapter(this, R.layout.todo_item, todolist)
        todoList.emptyView = helpText

        // Onclick listener for add button
        addBtn.setOnClickListener {
            // By pressing the add button, we will inflate an AlertDialog.
            val builder = AlertDialog.Builder(this) // new
            val dialogView = layoutInflater.inflate(R.layout.add_todo_dialog, null)

            // Get elements from custom dialog layout (add_todo_dialog.xml)
            val titleToAdd = dialogView.findViewById<EditText>(R.id.todoTitle) // 중요 , 변수(제목)
            val desciptionToAdd = dialogView.findViewById<EditText>(R.id.todoDescription) // 중요, 설명
            val dueDateToAdd = dialogView.findViewById<Button>(R.id.dueDate) // 버튼
            val datetextToAdd = dialogView.findViewById<TextView>(R.id.dateText) // dateText날짜를 보여주는 텍스트
            val finishedToAdd = dialogView.findViewById<CheckBox>(R.id.finishedToAdd) // 내가 설정한 Id 찾아줌(연결) 사용많음



            // Add InputMethodManager for auto keyboard popup
            val ime = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            dueDateToAdd.setOnClickListener{ // 파라미터 안씀 / ischecked 는 행동, 날짜는 행동이 아니라 정해진 것을 가져옴 그래서 다름
                ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0) //datepickerdialog 라이브러리 x
                val today = GregorianCalendar()
                val year: Int = today.get(Calendar.YEAR)
                val month: Int = today.get(Calendar.MONTH)
                val date: Int = today.get(Calendar.DATE)

                val dlg = DatePickerDialog(this, DatePickerDialog.OnDateSetListener
                { view, year, month, dayOfMonth -> datetextToAdd.text = "${year}년 ${month+1}월 ${dayOfMonth}일" }, year, month, date)
                dlg.show() // datetextToAdd 레이아웃 추가


            } // button 기능 함수 (70부터 여기까지)



            finishedToAdd.setOnCheckedChangeListener{_, ischecked -> // true,false
                finishedToAdd.isChecked = ischecked //isChecked 원래 있는 거 , 행동하면 선언한 ischecked에 들어감 t,f 값을 레이아웃에 전달
            }

            // Cursor auto focus on title when AlertDialog is inflated
            titleToAdd.requestFocus()

            // Show keyboard when AlertDialog is inflated
            ime.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)


            // Add positive button and negative button for AlertDialog.
            // Pressing the positive button: Add data to the database and also add them in listview and update.
            // Pressing the negative button: Do nothing. Close the AlertDialog
            val add = builder.setView(dialogView)
                .setPositiveButton("추가") { _, _ ->
                    if (!TextUtils.isEmpty(titleToAdd.text.trim())) {
                        // Add item to the database
                        val todo = Todo(
                            titleToAdd.text.toString(), // titletoadd (변수) , 내가 작성한 제목이 여기에 들어감 , 내가 작성한 글이 객체처럼 들어감
                            desciptionToAdd.text.toString(), // text만 뽑아서 db에
                            datetextToAdd.text.toString(),// date text만 있으면 됨 (쉼표도 꼭)
                        finishedToAdd.isChecked //todo 가 db table (,로 자료값구분) db에는 저장 됨 // 4가지 title ,description, date, finished
                        )

                        dbHandler!!.addTodo(todo)

                        // Add them to listview and update.
                        (todoList.adapter as TodoListViewAdapter).add(todo)
                        (todoList.adapter as TodoListViewAdapter).notifyDataSetChanged()

                        // Close keyboard
                        ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                    }
                    else {
                        Toast.makeText(this,
                            "제목을 입력하세요!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소") {_, _ ->
                    // Cancel Btn. Do nothing. Close keyboard.
                    ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                }
                .show()
                .getButton(DialogInterface.BUTTON_POSITIVE)

            // Default status of add button should be disabled. Because when AlertDialog inflates,
            // the title is empty by default and we do not want empty titles to be added to listview
            // and in databases.
            add.isEnabled = false

            // Listener for title text. If something is inputted in title, we should re-enable the add button.
            titleToAdd.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    if (!TextUtils.isEmpty(p0.toString().trim())) {
                        add.isEnabled = true
                    }
                    else {
                        titleToAdd.error = "TODO 제목을 입력하세요!"
                        add.isEnabled = false
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            })
        }
    }
}

