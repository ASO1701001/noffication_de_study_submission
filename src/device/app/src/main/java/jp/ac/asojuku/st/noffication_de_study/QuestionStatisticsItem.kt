package jp.ac.asojuku.st.noffication_de_study

class QuestionStatisticsItem {
    private var id: Long = 0
    private var title: String? = null
    private var rate: String? = null

    fun getId(): Long {
        return id
    }

    fun setId(id: Long) {
        this.id = id
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun getRate(): String? {
        return rate
    }

    fun setRate(rate: String) {
        this.rate = rate
    }

}