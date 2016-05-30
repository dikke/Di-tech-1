package ditech.feature

import com.houjp.common.io.IO
import com.houjp.ditech16
import com.houjp.ditech16.datastructure.{District, OrderAbs}
import ditech.common.util.Directory

object PreArrive {

  def main(args: Array[String]) {
    // 寻找往前 pre 个时间片的arrive
//    run(ditech16.s1_pt, 1)
//    run(ditech16.s1_pt, 2)
//    run(ditech16.s1_pt, 3)

    // 寻找往前 1~3 个时间arrive片
    run_ave(ditech16.s1_pt)
  }

  def run_ave(data_pt: String): Unit = {
    val districts_fp = data_pt + "/cluster_map/cluster_map"
    val districts = District.load_local(districts_fp)

    val date_fp = data_pt + "/dates"
    val dates = IO.load(date_fp).distinct

    dates.foreach { date =>
      val preArrive = collection.mutable.Map[(Int, Int), Double]()

      val preArrive_dir = data_pt + s"/fs/preArriveave"
      Directory.create( preArrive_dir )
      val preArrive_fp = preArrive_dir +  s"/preArriveave_$date"

      val order_abs_fp = data_pt + s"/order_abs_data/order_data_$date"
      val orders_abs = OrderAbs.load_local(order_abs_fp)


      val preArrive_1 = cal_pre_Arrive(orders_abs, 1)
      preArrive_1.foreach { e =>
        preArrive(e._1) = preArrive.getOrElse(e._1, 0.0) + 0.333 * e._2
      }

      val preArrive_2 = cal_pre_Arrive(orders_abs, 2)
      preArrive_2.foreach { e =>
        preArrive(e._1) = preArrive.getOrElse(e._1, 0.0) + 0.333 * e._2
      }

      val preArrive_3 = cal_pre_Arrive(orders_abs, 3)
      preArrive_3.foreach { e =>
        preArrive(e._1) = preArrive.getOrElse(e._1, 0.0) + 0.333 * e._2
      }

      val preArrive_s = districts.values.toArray.sorted.flatMap { did =>
        Range(1, 145).map { tid =>
          val v = preArrive.getOrElse((did, tid), 0.0)
          s"$did,$tid\t$v"
        }
      }
      IO.write(preArrive_fp, preArrive_s)
    }
  }

  def run(data_pt: String, pre: Int): Unit = {
    val districts_fp = data_pt + "/cluster_map/cluster_map"
    val districts = District.load_local(districts_fp)

    val date_fp = data_pt + "/dates"
    val dates = IO.load(date_fp).distinct

    dates.foreach { date =>
      val order_abs_fp = data_pt + s"/order_abs_data/order_data_$date"
      val orders_abs = OrderAbs.load_local(order_abs_fp)

      val preArrive_dir= data_pt + s"/fs/preArrive_$pre"
      Directory.create( preArrive_dir )
      val preArrive_fp = preArrive_dir + s"/preArrive_${pre}_$date"

      val preArrive = cal_pre_Arrive(orders_abs, pre)

      val preArrive_s = districts.values.toArray.sorted.flatMap { did =>
        Range(1, 145).map { tid =>
          val v = preArrive.getOrElse((did, tid), 0.0)
          s"$did,$tid\t$v"
        }
      }
      IO.write(preArrive_fp, preArrive_s)
    }
  }

  def cal_pre_Arrive(orders: Array[OrderAbs], t_len: Int): Map[(Int, Int), Double] = {
    val tid_len = 144
    val fs = collection.mutable.Map[(Int, Int), Double]()

    orders.foreach { e =>
      if (-1 != e.dest_district_id &&
        e.has_driver &&
        (tid_len >= e.time_id + t_len) &&
        (1 <= e.time_id + t_len)) {
        fs((e.dest_district_id, e.time_id + t_len)) = fs.getOrElse((e.dest_district_id, e.time_id + t_len), 0.0) +1.0
      }
    }

    fs.toMap
  }
}