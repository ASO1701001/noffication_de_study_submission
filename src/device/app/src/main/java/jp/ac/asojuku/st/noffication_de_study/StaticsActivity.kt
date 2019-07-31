package jp.ac.asojuku.st.noffication_de_study

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import jp.ac.asojuku.st.noffication_de_study.db.AnswersRateOpenHelper
import kotlinx.android.synthetic.main.activity_statics.*

class StaticsActivity : AppCompatActivity(), FragmentMyRecord.OnFragmentInteractionListener,
    FragmentQuestion.OnFragmentInteractionListener {
    override fun onFragmentInteraction(uri: Uri) {}

    private val tabTitle = arrayOf<CharSequence>("my記録", "問題ごと")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statics)

        val adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment? {
                return when (position) {
                    0 -> FragmentMyRecord()
                    1 -> {
                        val fragment = FragmentQuestion()
                        fragment.arguments = Bundle().apply {
                            val dbHelper = AnswersRateOpenHelper(SQLiteHelper(this@StaticsActivity).readableDatabase)
                            val data = dbHelper.find_all_rate()
                            putStringArrayList("DATA", data)
                        }
                        return fragment
                    }
                    else -> null
                }
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return tabTitle[position]
            }

            override fun getCount(): Int {
                return tabTitle.size
            }
        }

        val viewPager = viewPager
        viewPager.offscreenPageLimit = tabTitle.size
        viewPager.adapter = adapter
        val tabLayout = tabLayout
        tabLayout.setupWithViewPager(viewPager)

        SA_Back_BTN.setSafeClickListener {
            finish()
        }
    }
}
