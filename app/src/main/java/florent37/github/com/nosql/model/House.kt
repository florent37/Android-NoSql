package florent37.github.com.nosql.model

class House {
    var adress: String? = null

    constructor() {}
    constructor(adress: String?) {
        this.adress = adress
    }

    override fun toString(): String {
        return "House {" + '\n' +
                "- adress :" + adress + '\n' +
                '}'
    }
}