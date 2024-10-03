#include <map>

typedef std::pair<char*,double> Entry;

class Record {
public:
    Record() {

    }

    template <class... T>
    Record(Entry entry, T... rest): Record(rest...) {
        mymap.insert(entry);
    }

    void put(char* k, double v) {
        mymap.insert(Entry(k, v));
    }

    String asJson() const {
        String str = "{";
        for (auto it=mymap.begin(); it!=mymap.end(); ++it) {
            str += "\"";
            str += it->first;
            str += "\":";
            str += it->second;
            if (it != std::prev(mymap.end())) {
                str += ",";
            }
        }
        str += "}";
        return str;
    }

    String asNetpieStr() const {
        String str = "{";
        for (auto it=mymap.begin(); it!=mymap.end(); ++it) {
            str += it->first;
            str += ":";
            str += it->second;
            if (it != std::prev(mymap.end())) {
                str += ",";
            }
        }
        str += "}";
        return str;
    }
private:
    std::map<char*, double> mymap;
};


// DATA:    [===       ]  33.9% (used 27748 bytes from 81920 bytes)
// PROGRAM: [==        ]  24.1% (used 252040 bytes from 1044464 bytes)