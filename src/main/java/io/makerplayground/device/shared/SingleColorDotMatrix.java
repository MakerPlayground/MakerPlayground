/*
 * Copyright (c) 2020. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.device.shared;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javafx.beans.property.ReadOnlyBooleanWrapper;

@JsonSerialize(using = SingleColorDotMatrixSerializer.class)
@JsonDeserialize(using = SingleColorDotMatrixDeserializer.class)
public class SingleColorDotMatrix implements DotMatrix {
    int row;
    int column;
    ReadOnlyBooleanWrapper[][] data;

    public SingleColorDotMatrix() {
        this.row = 8;
        this.column = 8;
        this.data = createMatrix(this.row, this.column);
    }

    public SingleColorDotMatrix(int row, int column, String data) {
        this.row = row;
        this.column = column;
        this.data = decodeBase16String(data);
    }

    private static ReadOnlyBooleanWrapper[][] createMatrix(int row, int column) {
        if (row <= 0 || column <= 0) {
            throw new IndexOutOfBoundsException();
        }
        ReadOnlyBooleanWrapper[][] data = new ReadOnlyBooleanWrapper[row][];
        for (int i=0; i<row; i++) {
            data[i] = new ReadOnlyBooleanWrapper[column];
            for (int j=0; j<column; j++) {
                data[i][j] = new ReadOnlyBooleanWrapper(false);
            }
        }
        return data;
    }

    private static ReadOnlyBooleanWrapper[][] resizeMatrix(ReadOnlyBooleanWrapper[][] currentMatrix, int row, int column) {
        ReadOnlyBooleanWrapper[][] data = createMatrix(row, column);
        for (int i=0; i<Math.min(row, currentMatrix.length); i++) {
            System.arraycopy(currentMatrix[i], 0, data[i], 0, Math.min(currentMatrix[i].length, data[i].length));
        }
        return data;
    }

    public void resize(int row, int column) {
        this.row = row;
        this.column = column;
        this.data = resizeMatrix(this.data, row, column);
    }

    public void set(int row, int column, Boolean val) {
        if (row < 0 || row >= this.row || column < 0 || column >= this.column) {
            throw new IndexOutOfBoundsException();
        }
        this.data[row][column].set(val);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public ReadOnlyBooleanWrapper[][] getData() {
        return data;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static String encodeBase16String(ReadOnlyBooleanWrapper[][] data) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<data.length; i++) {
            int k = 0, val = 0;
            for (int j=0; j<data[i].length; j++, k++) {
                val = (val << 1) | (data[i][j].get() ? 1 : 0);
                if (k==3) {
                    sb.append(HEX_ARRAY[val]);
                    val = 0;
                    k = -1;
                }
            }
            if (k != 0) {
                for(; k != 3; k++) {
                    val = (val << 1);
                }
                sb.append(HEX_ARRAY[val]);
            }
            if (data.length != 1 && i < data.length - 1) {
                sb.append('&');
            }
        }
        return sb.toString();
    }

    private static ReadOnlyBooleanWrapper[][] decodeBase16String(String data) {
        String[] lines = data.split("&");
        ReadOnlyBooleanWrapper[][] retVal = new ReadOnlyBooleanWrapper[lines.length][];
        for (int r=0; r<lines.length; r++) {
            retVal[r] = new ReadOnlyBooleanWrapper[lines[r].length() * 4];
            for (int c=0; c<lines[r].length(); c++) {
                int value = Integer.parseInt(Character.toString(lines[r].charAt(c)), 16);
                retVal[r][4*c]   = new ReadOnlyBooleanWrapper((value & 0B00001000) > 0);
                retVal[r][4*c+1] = new ReadOnlyBooleanWrapper((value & 0B00000100) > 0);
                retVal[r][4*c+2] = new ReadOnlyBooleanWrapper((value & 0B00000010) > 0);
                retVal[r][4*c+3] = new ReadOnlyBooleanWrapper((value & 0B00000001) > 0);
            }
        }
        return retVal;
    }

    public String getDataAsString() {
        return encodeBase16String(this.data);
    }
}
