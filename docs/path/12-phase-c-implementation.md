# 12: Phase C 実装（Unit 05: 勤怠修正 / Unit 06: 月次集計・帳票）

## プロンプト

> Phase C に移行

## 選択肢への回答

PDF 出力について JasperReports 以外の選択肢を提示したが：

> 実際のSIerプロジェクトに沿わせたいので、JasperReportsじゃない？

と回答を受け、JasperReports で実装した。

## やったこと

### 1. Phase C 計画提示 → 承認

Phase B 完了を確認し、以下の計画を提示して承認を得た：

1. Flyway V4 マイグレーション（corrections テーブル）
2. Unit 05 Backend TDD（Enum → Entity → DTO → Repository → Service → Controller）
3. Unit 06 Backend TDD（DTO → ReportService → CsvExportService → PdfExportService → Controller）
4. Unit 05 / 06 Frontend（並列 subagent）
5. Sidebar ナビゲーション更新

### 2. Flyway V4 マイグレーション

`V4__create_attendance_corrections.sql` を作成。テーブル定義に `reject_reason` カラムを追加（Unit 定義にはなかったが、却下理由の保存に必要）。

### 3. Unit 05: 勤怠修正（Backend TDD）

| ファイル | 役割 |
|---------|------|
| `CorrectionStatus.java` | Enum（PENDING / APPROVED / REJECTED） |
| `AttendanceCorrection.java` | Entity（@Version 楽観ロック対応） |
| `CorrectionCreateRequest.java` | 申請リクエスト DTO |
| `CorrectionRejectRequest.java` | 却下リクエスト DTO |
| `CorrectionResponse.java` | レスポンス DTO（`from()` ファクトリメソッド） |
| `PendingCorrectionResponse.java` | 承認待ち一覧レスポンス DTO |
| `AttendanceCorrectionRepository.java` | Spring Data JPA Repository |
| `CorrectionService.java` | Service interface |
| `CorrectionServiceImpl.java` | Service 実装 |
| `CorrectionController.java` | REST Controller |

テストケース（Service: 10件、Controller: 5件）：

- 修正申請作成（既存レコード / 打刻忘れ）
- 自分の申請一覧（全件 / ステータスフィルタ）
- 承認待ち一覧（自部署の PENDING のみ）
- 承認（既存レコード更新 / 打刻忘れ新規作成）
- 承認権限チェック（他部署上長は 403）
- 上長自己承認
- 却下（理由保存）
- 楽観ロック（バージョン不一致で 409）

### 4. Unit 06: 月次集計・帳票（Backend TDD）

| ファイル | 役割 |
|---------|------|
| `EmployeeMonthlyRecord.java` | 社員ごとの月次集計 DTO |
| `MonthlyReportResponse.java` | 月次レポートレスポンス DTO |
| `ReportService.java` | Service interface |
| `ReportServiceImpl.java` | 月次集計ロジック（WorkDuration 再利用） |
| `CsvExportService.java` / `Impl` | CSV エクスポート（UTF-8 BOM 付き） |
| `PdfExportService.java` / `Impl` | PDF エクスポート（JasperReports 7.x） |
| `ReportController.java` | REST Controller（JSON / CSV / PDF） |

テストケース（ReportService: 2件、CsvExport: 2件、PdfExport: 1件、Controller: 3件）：

- 全社員月次集計
- 部署フィルタ
- CSV ヘッダー・データ行
- CSV UTF-8 BOM
- PDF バイト配列生成
- Controller（JSON / CSV / PDF レスポンス）

### 5. JasperReports 7.x 対応

JasperReports 7.x は以前の digester ベースから **Jackson XML** ベースに移行しており、jrxml フォーマットが大幅に変わっていた。

#### つまずき

1. **名前空間の拒否**: JR 7.x の `JacksonReportLoader.detectRootElement()` は `xmlns` 付き要素を検出しない。バイトコード解析で `getNamespaceURI() != null && !isEmpty()` で `false` を返すことを確認
2. **フラット属性形式**: `<band>` ネスト、`<reportElement>`, `<textElement>` サブ要素は使えない。属性は要素に直接指定する
3. **element kind 属性**: `<textField>` → `<element kind="textField">` に変更
4. **Java record 非対応**: `JRBeanCollectionDataSource` は JavaBeans の `getXxx()` を期待。record の `xxx()` アクセサは認識されない → `JRMapCollectionDataSource` に切り替え

#### JR 7.x jrxml フォーマット（抜粋）

```xml
<!-- JR 6.x（旧形式） -->
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports" ...>
  <title>
    <band height="50">
      <textField>
        <reportElement x="0" y="0" width="100" height="20"/>
        <textElement textAlignment="Center"/>
        <textFieldExpression><![CDATA[$F{name}]]></textFieldExpression>
      </textField>
    </band>
  </title>

<!-- JR 7.x（新形式） -->
<jasperReport name="report" ...>  <!-- xmlns なし -->
  <title height="50">  <!-- band なし -->
    <element kind="textField" x="0" y="0" width="100" height="20"
             hTextAlign="Center">  <!-- フラット属性 -->
      <expression><![CDATA[$F{name}]]></expression>
    </element>
  </title>
```

### 6. Gradle 依存追加

```kotlin
implementation("net.sf.jasperreports:jasperreports:7.0.1")
implementation("net.sf.jasperreports:jasperreports-pdf:7.0.1")
implementation("net.sf.jasperreports:jasperreports-jdt:7.0.1")
```

JR 7.x ではコアと PDF / JDT コンパイラが別モジュールに分離されている。

### 7. Frontend（subagent 並列実装）

Unit 05 と Unit 06 のフロントエンドを 2 つの subagent で並列作成。

#### Unit 05 Frontend

| ファイル | 役割 |
|---------|------|
| `correction-api.ts` | API クライアント + 型定義 |
| `useCorrections.ts` | TanStack Query hooks |
| `CorrectionList.tsx` | 修正申請一覧（ステータスフィルタ付き） |
| `CorrectionForm.tsx` | 修正申請フォーム |
| `PendingCorrectionList.tsx` | 承認待ち一覧（上長向け） |
| `ApprovalActions.tsx` | 承認 / 却下ボタン |
| `/corrections/page.tsx` | 修正申請一覧ページ |
| `/corrections/new/page.tsx` | 新規修正申請ページ |
| `/approvals/page.tsx` | 承認ページ（上長のみ） |

#### Unit 06 Frontend

| ファイル | 役割 |
|---------|------|
| `report-api.ts` | API クライアント + ダウンロードヘルパー |
| `useReports.ts` | TanStack Query hooks |
| `MonthlyReportTable.tsx` | 月次集計テーブル |
| `DepartmentFilter.tsx` | 部署フィルタ |
| `ExportButtons.tsx` | CSV / PDF ダウンロードボタン |
| `/admin/reports/page.tsx` | 月次集計ページ（管理者のみ） |

### 8. Sidebar ナビゲーション更新

- 共通メニューに「修正申請」追加
- 上長メニューセクション追加（「承認」）
- 管理メニューに「月次集計」追加

### 9. 品質チェック

- `gradlew check`: テスト + Checkstyle + SpotBugs 全パス
- `next build`: TypeScript 型チェック + ビルド成功
- `vitest run`: 53 テスト全パス

## 最終構成（追加分）

```
packages/backend/src/
├── main/java/com/example/attendance/
│   ├── correction/
│   │   ├── controller/CorrectionController.java
│   │   ├── dto/
│   │   │   ├── CorrectionCreateRequest.java
│   │   │   ├── CorrectionRejectRequest.java
│   │   │   ├── CorrectionResponse.java
│   │   │   └── PendingCorrectionResponse.java
│   │   ├── entity/
│   │   │   ├── AttendanceCorrection.java
│   │   │   └── CorrectionStatus.java
│   │   ├── repository/AttendanceCorrectionRepository.java
│   │   └── service/
│   │       ├── CorrectionService.java
│   │       └── CorrectionServiceImpl.java
│   └── report/
│       ├── controller/ReportController.java
│       ├── dto/
│       │   ├── EmployeeMonthlyRecord.java
│       │   └── MonthlyReportResponse.java
│       └── service/
│           ├── CsvExportService.java
│           ├── CsvExportServiceImpl.java
│           ├── PdfExportService.java
│           ├── PdfExportServiceImpl.java
│           ├── ReportService.java
│           └── ReportServiceImpl.java
├── main/resources/
│   ├── db/migration/V4__create_attendance_corrections.sql
│   └── reports/monthly-report.jrxml
└── test/java/com/example/attendance/
    ├── correction/
    │   ├── controller/CorrectionControllerTest.java
    │   └── service/CorrectionServiceTest.java
    └── report/
        ├── controller/ReportControllerTest.java
        └── service/
            ├── CsvExportServiceTest.java
            ├── PdfExportServiceTest.java
            └── ReportServiceTest.java

packages/frontend/src/
├── features/
│   ├── correction/
│   │   ├── ApprovalActions.tsx
│   │   ├── CorrectionForm.tsx
│   │   ├── CorrectionList.tsx
│   │   ├── PendingCorrectionList.tsx
│   │   ├── correction-api.ts
│   │   └── useCorrections.ts
│   └── report/
│       ├── DepartmentFilter.tsx
│       ├── ExportButtons.tsx
│       ├── MonthlyReportTable.tsx
│       ├── report-api.ts
│       └── useReports.ts
├── app/(authenticated)/
│   ├── corrections/
│   │   ├── page.tsx
│   │   └── new/page.tsx
│   ├── approvals/page.tsx
│   └── admin/reports/page.tsx
└── components/layout/Sidebar.tsx（更新）
```
