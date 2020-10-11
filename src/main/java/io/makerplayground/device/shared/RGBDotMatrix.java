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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.paint.Color;
import org.apache.commons.codec.binary.Base64OutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@JsonSerialize(using = RGBDotMatrixSerializer.class)
@JsonDeserialize(using = RGBDotMatrixDeserializer.class)
public class RGBDotMatrix implements DotMatrix {
    int row;
    int column;
    ReadOnlyObjectWrapper<Color>[][] data;

    public RGBDotMatrix() {
        this.row = 8;
        this.column = 8;
        this.data = createMatrix(this.row, this.column);
    }

    public RGBDotMatrix(int row, int column, String data) {
        this.row = row;
        this.column = column;
        this.data = decodeBase64String(data);
    }

    private static ReadOnlyObjectWrapper<Color>[][] createMatrix(int row, int column) {
        if (row <= 0 || column <= 0) {
            throw new IndexOutOfBoundsException();
        }
        ReadOnlyObjectWrapper<Color>[][] data = new ReadOnlyObjectWrapper[row][];
        for (int i=0; i<row; i++) {
            data[i] = new ReadOnlyObjectWrapper[column];
            for (int j=0; j<column; j++) {
                data[i][j] = new ReadOnlyObjectWrapper<>(Color.BLACK);
            }
        }
        return data;
    }

    private static ReadOnlyObjectWrapper<Color>[][] resizeMatrix(ReadOnlyObjectWrapper<Color>[][] currentMatrix, int newRowCount, int newColumnCount) {
        int currentRow = currentMatrix.length;
        int currentColumn = currentMatrix.length > 0 ? currentMatrix[0].length : 0;
        ReadOnlyObjectWrapper<Color>[][] data = createMatrix(newRowCount, newColumnCount);
        for (int i=0; i<Math.min(newRowCount, currentRow); i++) {
            data[i] = new ReadOnlyObjectWrapper[newColumnCount];
            for (int j=0; j<Math.min(newColumnCount, currentColumn); j++) {
                data[i][j] = new ReadOnlyObjectWrapper<>(currentMatrix[i][j].get());
            }
        }
        return data;
    }

    public void resize(int row, int column) {
        this.row = row;
        this.column = column;
        this.data = resizeMatrix(this.data, row, column);
    }

    public void set(int row, int column, Color val) {
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

    public ReadOnlyObjectWrapper<Color>[][] getData() {
        return data;
    }

    private static String encodeBase64String(ReadOnlyObjectWrapper<Color>[][] data) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Base64OutputStream outputStream = new Base64OutputStream(bos, true, 0, new byte[0])) {
            int rowCount = data.length;
            int colCount = data[0].length;
            outputStream.write(rowCount);
            outputStream.write(colCount);
            outputStream.write(0);
            for (var colData : data) {
                if (colData.length != colCount) {
                    throw new IllegalStateException("Column size mismatch!!!");
                }
                for (var color : colData) {
                    outputStream.write((int) (color.get().getRed() * 255));
                    outputStream.write((int) (color.get().getGreen() * 255));
                    outputStream.write((int) (color.get().getBlue() * 255));
                }
            }
            outputStream.flush();
            return bos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Can't encode data!!!");
        }
    }

    private static ReadOnlyObjectWrapper<Color>[][] decodeBase64String(String data) {
        byte[] byteData = Base64.getDecoder().decode(data);
        if (byteData.length % 3 != 0) {
            throw new IllegalStateException("Data is not valid");
        }

        int row = byteData[0];
        int col = byteData[1];
        // we skip byteData[2] as it is there just to align the first color data to the fifth base64 character

        int currentIndex = 3;
        ReadOnlyObjectWrapper<Color>[][] colorData = new ReadOnlyObjectWrapper[row][col];
        for (int r=0; r<row; r++) {
            colorData[r] = new ReadOnlyObjectWrapper[col];
            for (int c=0; c<col; c++) {
                colorData[r][c] = new ReadOnlyObjectWrapper<>(Color.rgb(Byte.toUnsignedInt(byteData[currentIndex])
                        , Byte.toUnsignedInt(byteData[currentIndex+1]), Byte.toUnsignedInt(byteData[currentIndex+2])));
                currentIndex += 3;
            }
        }
        return colorData;
    }

    public String getDataAsString() {
        return encodeBase64String(this.data);
    }
}
