package com.d103.asaf.ui.library.pro

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import co.nedim.maildroidx.MaildroidX
import co.nedim.maildroidx.MaildroidXType
import com.d103.asaf.R
import com.d103.asaf.common.config.BaseFragment
import com.d103.asaf.common.model.dto.Book
import com.d103.asaf.common.model.dto.Classinfo
import com.d103.asaf.common.util.RetrofitUtil
import com.d103.asaf.databinding.DialogAddBookBinding
import com.d103.asaf.databinding.FragmentLibraryManagementBinding
import com.d103.asaf.ui.library.LibraryFragmentViewModel
import com.d103.asaf.ui.library.adapter.BookAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val PATH = "/data/data/com.d103.asaf/"

// String -> BookDto로 변경 필요
class LibraryManagementFragment : BaseFragment<FragmentLibraryManagementBinding>(
    FragmentLibraryManagementBinding::bind,
    R.layout.fragment_library_management
) {
    private val viewModel: LibraryFragmentViewModel by viewModels()
    private var books: MutableList<Book> = mutableListOf()
    private lateinit var adapter: BookAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initClass()
        initView()
    }

    private fun initList() {
        books = viewModel.returns.value
        Log.d("책", "initList: $books")
        adapter = BookAdapter()
        adapter.isDraw = true
        binding.fragmentLibraryRecyclerview.adapter = adapter
        adapter.submitList(books)
    }

    private fun initView() {
        binding.apply {
            bookToggleButton.seatText.text = "대출 현황"
            bookToggleButton.lockerText.text = "전체 도서 목록"
            bookToggleButton.moneyText.visibility = View.GONE

            bookToggleButton.setFirstButtonClickListener {
                adapter.isDraw = true
                fragmentLibraryTextviewSecond.text = "대출자"
                fragmentLibraryTextviewThird.text = "반납일"
                books = viewModel.returns.value
                adapter.submitList(books)
            }

            bookToggleButton.setSecondButtonClickListener {
                adapter.isDraw = false
                fragmentLibraryTextviewSecond.text = "저자"
                fragmentLibraryTextviewThird.text = "수량"
                books = viewModel.books.value
                adapter.submitList(books)
            }

            fragmentLibrarySearchBar.setSearchClickListener {
                fragmentLibrarySearchBar.searchEditText.text.clear()
            }

            fragmentLibrarySearchBar.searchEditText.addTextChangedListener(searchWatcher)

            fragmentLibraryRecyclerview.isVisible = true

            fragmentLibraryFabAddbook.setOnClickListener {
                addBookDialog()
            }
        }
    }

    private fun initClass() {
        binding.apply {
            fragmentLibraryDropdownClass.dropdownText.addTextChangedListener(classWatcher)
            fragmentLibraryDropdownClass.dropdownText.text =
                viewModel.classSurfaces.value[0].toString()
            fragmentLibraryDropdownClass.dropdownTextPost.text = "반"
            // 객체가 바뀌면 안됨.. 요소를 변경해줘야 변화 인식됨
            fragmentLibraryDropdownClass.dataList.addAll(viewModel.classSurfaces.value)
            fragmentLibraryDropdownClass.dataList.removeAt(0)
        }
    }

    private val classWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // 텍스트가 변경되기 전에 호출됩니다.
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            // 텍스트가 변경된 후에 호출됩니다.
            viewModel.curClass.value =
                Classinfo(
                    viewModel.curClass.value.classNum,
                    s.toString().toInt(), viewModel.curClass.value.regionCode,
                    viewModel.curClass.value.generationCode, viewModel.curClass.value.userId
                )
        }
    }

    private val searchWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // 텍스트가 변경되기 전에 호출됩니다.
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            bookSearch(s.toString())
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    private fun bookSearch(title: String) {
        val filteredBooks = books.filter { book -> book.bookName.contains(title) }
        adapter.submitList(filteredBooks)
    }

    // DB에 저장할 때 Date 타입
    private fun getDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 3)
        return calendar.time
    }

    // 텍스트에 사용할 때 String 타입
    private fun setDate(loanPeriod: Int): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.KOREA)
        val currentDate = Calendar.getInstance()
        currentDate.add(Calendar.DAY_OF_MONTH, loanPeriod)
        return dateFormat.format(currentDate.time)
    }


    private fun addBookDialog() {
        val dialogView = DialogAddBookBinding.inflate(layoutInflater, binding.root, false)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView.root)
            .create()

        // 도서 등록 기능 POST
        dialogView.bookAdd.setOnClickListener {
            // 입력받은 정보 DTO로 조립 & POST & dismiss() & createQR % send email
            // val bookDto = ~
            val book = Book(
                bookName = dialogView.dialogAddBookEdittextTitle.text.toString(),
                author = dialogView.dialogAddBookEdittextAuthor.text.toString(),
                publisher = dialogView.dialogAddBookEdittextPublisher.text.toString()
            )
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val qr = viewModel.generateQRCode(book.bookName, book.author, book.publisher)
                    val qrImg = PATH + "${getFileName(book.bookName)}.png"
                    viewModel.saveQRCode(qr, qrImg)
                    sendEmail(qrImg)
                    postBook(book)
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                Log.d("메일전송", "addBookDialog: DDDddd")
                e.printStackTrace()
            }
        }
        dialogView.bookCancel.setOnClickListener {
            dialog.dismiss()
        }
        // 다이얼로그 키보드 띄워지면 resizing 하는 코드
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Set the dialog's window to not fit system windows
                dialog.window?.setDecorFitsSystemWindows(false)

                // Set the dialog's window to listen for window insets
                ViewCompat.setOnApplyWindowInsetsListener(dialogView.root) { v, insets ->
                    // Get the bottom inset of the system bar
                    val bottomInset = insets.getInsets(WindowInsets.Type.ime()).bottom

                    // Set the dialog's padding to the bottom inset
                    v.setPadding(0, 0, 0, bottomInset)

                    insets
                }
            } else {
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun getFileName(title: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        return "$title ${sdf.format(Date())}"
    }

    private fun sendEmail(path: String) {
        MaildroidX.Builder()
            .smtp("smtp.mailtrap.live")
            .smtpUsername("api")
            .smtpPassword("0647ceab68282d673bdd53a351635833")
            .port("587")
            .type(MaildroidXType.HTML)
            .to("kieanupark@gmail.com")
            .from("mailtrap@asaf.live")
            .subject("hello")
            .body("body")
            .attachment(path)
            .isStartTLSEnabled(true)
            .mail()

        Log.d("메일", "sendEmail: 보냄")
    }


    // 책 등록
    private fun postBook(book: Book) {
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitUtil.libraryService.postBook(book)
        }
    }
}